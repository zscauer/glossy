package com.github.zscauer.glsy.security;

import com.github.zscauer.glsy.common.ActiveRecord;
import com.github.zscauer.glsy.common.RequestParams;
import com.github.zscauer.glsy.common.TemplateUsage;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.NewCookie;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * Profile of content access.
 * <p>
 * Define accessible content through available {@linkplain Privilege privileges} for {@linkplain AccessMode access mode}, linked to active profile.
 */
@Slf4j
public abstract sealed class AccessProfile extends ActiveRecord permits ObserverProfile, ModeratorProfile, AdminProfile {

    protected static final SecretKeyFactory SECRET_KEY_FACTORY;

    public static final String COOKIE_ACCESS_MODE_SECRET = "GLSY-ACCESS-MODE-SECRET";
    protected static final NewCookie.Builder ACCESS_MODE_COOKIE_BUILDER = new NewCookie.Builder(COOKIE_ACCESS_MODE_SECRET);

    static {
        try {
            SECRET_KEY_FACTORY = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return role of current content access profile
     */
    public abstract AccessMode accessMode();

    /**
     * @param requestedContent   type of controlled content
     * @param requestedPrivilege privilege of access
     * @return {@code true} if profile with this privilege have access to requested content
     */
    @TemplateUsage
    @SuppressWarnings("unused")
    public abstract boolean havePrivilegesToContent(AccessibleContent requestedContent, Privilege requestedPrivilege);

    /**
     * Define that user authorized for current profile by their request.
     *
     * @param requestParams parameters of user request
     * @return {@code true} if user authorized with this profile
     */
    public abstract boolean authorized(@Nullable RequestParams requestParams);

    /**
     * Authorize user for current profile.
     *
     * @param secret          secret key, inputed by user
     * @param requestParams   parameters of user request
     * @param responseBuilder builder of response to user request
     * @throws GrantAccessException if user cannot be authorized for current profile
     */
    public abstract void grant(String secret, RequestParams requestParams, ResponseBuilder<?> responseBuilder) throws GrantAccessException;

    /**
     * Deauthorize user for current profile.
     *
     * @param responseBuilder builder of response to user request
     */
    public abstract void revoke(ResponseBuilder<?> responseBuilder);

    protected static String generateEncryptedSecret(final String secret, final byte[] salt) throws InvalidKeySpecException {
        return 1000 + ":" + toHex(salt) + ":" + toHex(SECRET_KEY_FACTORY.generateSecret(createNewPBEKeySpec(secret, salt)).getEncoded());
    }

    protected static PBEKeySpec createNewPBEKeySpec(final String secret, final byte[] salt) {
        return new PBEKeySpec(secret.toCharArray(), salt, 1000, 64 * 8);
    }

    protected static String toHex(final byte[] array) {
        final String hex = new BigInteger(1, array).toString(16);
        final int paddingLength = (array.length * 2) - hex.length();

        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    protected static byte[] fromHex(final String hex) {
        final byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    protected boolean inputedSecretIsCorrect(@Nonnull final PBEKeySpec inputedKeySpec, @Nonnull final String encryptedCorrectSecret)
            throws InvalidKeySpecException {
        Objects.requireNonNull(inputedKeySpec, "Inputed key spec not provided.");
        Objects.requireNonNull(encryptedCorrectSecret, "Correct secret not provided.");

        final byte[] inputedSecretHash = SECRET_KEY_FACTORY.generateSecret(inputedKeySpec).getEncoded();
        if (inputedSecretHash.length < 1) {
            return false;
        }

        final String[] parts = encryptedCorrectSecret.split(":");
        final byte[] correctSecretHash = fromHex(parts[2]);

        int diff = correctSecretHash.length ^ inputedSecretHash.length;
        for (int i = 0; i < correctSecretHash.length && i < inputedSecretHash.length; i++) {
            diff |= correctSecretHash[i] ^ inputedSecretHash[i];
        }

        return diff == 0;
    }

    protected void addAccessCookie(final String value, final ResponseBuilder<?> responseBuilder) {
        responseBuilder.cookie(ACCESS_MODE_COOKIE_BUILDER.value(value).path("/").build());
    }

    // +++ Repository +++
    protected static void fillPrivileges(final Map<AccessibleContent, Privilege> previleges, final AccessMode accessMode) {
        ResultSet rs = null;
        try (final Connection connection = dataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(STATEMENT_FIND)) {
            statement.setString(1, accessMode.name());

            rs = statement.executeQuery();
            while (rs.next()) {
                previleges.put(
                        AccessibleContent.valueOf(rs.getString("accessible_content")),
                        Privilege.valueOf(rs.getString("privilege")));
            }
            closeResultSet(rs);
        } catch (final SQLException e) {
            closeResultSet(rs);
            log.error(e.getMessage());
        }
    }
    // --- Repository ---

    // +++ Mapping +++

    // --- Mapping ---

    // +++ Statements +++
    private static final String STATEMENT_FIND = """
            SELECT * FROM access_modes_content_privileges
            WHERE access_mode = CAST(? AS access_mode)
            """;
    // --- Statements ---

}
