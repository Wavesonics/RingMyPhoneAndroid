package com.darkrockstudios.apps.ringmyphone;

/**
 * Created by Adam on 12/10/13.
 */
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * BillingSecurity-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must performVerify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class BillingSecurity
{
    private static final String TAG = BillingSecurity.class.getSimpleName();

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM   = "SHA1withRSA";

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the PurchaseState
     * and product ID of the purchase.
     *
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData      the signed JSON string (signed, not encrypted)
     * @param signature       the signature for the data, signed with the private key
     */
    public static boolean verifySignature( final String base64PublicKey, final String signedData, final String signature )
    {
        final boolean verified;

        if( TextUtils.isEmpty( signedData ) || TextUtils.isEmpty( base64PublicKey ) ||
                TextUtils.isEmpty( signature ) )
        {
            Log.e( TAG, "Purchase verification failed: missing data." );
            verified = false;
        }
        else
        {
            PublicKey key = BillingSecurity.unpackPublicKey( base64PublicKey );
            verified = BillingSecurity.performVerify( key, signedData, signature );
        }
        return verified;
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    public static PublicKey unpackPublicKey( final String encodedPublicKey )
    {
        try
        {
            byte[] decodedKey = Base64.decode( encodedPublicKey, 0 );
            KeyFactory keyFactory = KeyFactory.getInstance( KEY_FACTORY_ALGORITHM );
            return keyFactory.generatePublic( new X509EncodedKeySpec( decodedKey ) );
        }
        catch( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
        catch( InvalidKeySpecException e )
        {
            Log.e( TAG, "Invalid key specification." );
            throw new IllegalArgumentException( e );
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey  public key associated with the developer account
     * @param signedData signed data from server
     * @param signature  server signature
     * @return true if the data and signature match
     */
    public static boolean performVerify( final PublicKey publicKey, final String signedData, final String signature )
    {
        boolean verified = false;

        Signature sig;
        try
        {
            sig = Signature.getInstance( SIGNATURE_ALGORITHM );
            sig.initVerify( publicKey );
            sig.update( signedData.getBytes() );
            if( sig.verify( Base64.decode( signature, 0 ) ) )
            {
                verified = true;
            }
            else
            {
                Log.e( TAG, "Signature verification failed." );
            }
        }
        catch( NoSuchAlgorithmException | InvalidKeyException | SignatureException e )
        {
            Log.e( TAG, e.getMessage() );
        }

        return verified;
    }

    public static String sha1Hash( final String toHash )
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes( "UTF-8" );
            digest.update( bytes, 0, bytes.length );
            bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for( byte b : bytes )
            {
                sb.append( String.format( "%02X", b ) );
            }
            hash = sb.toString();
        }
        catch( NoSuchAlgorithmException | UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }

        return hash;
    }

    public static String xorString( final String input, final char[] key )
    {
        final StringBuilder builder = new StringBuilder();
        for( int ii = 0; ii < input.length(); ++ii )
        {
            final int keySegment = key[ ii % key.length ];
            final char c = input.charAt( ii );

            builder.append( c ^ keySegment );
        }

        return builder.toString();
    }

    public static String superSecureCrypto( final String input )
    {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < input.length(); i++ )
        {
            char c = input.charAt( i );
            if( c >= 'a' && c <= 'm' )
            {
                c += 13;
            }
            else if( c >= 'A' && c <= 'M' )
            {
                c += 13;
            }
            else if( c >= 'n' && c <= 'z' )
            {
                c -= 13;
            }
            else if( c >= 'N' && c <= 'Z' )
            {
                c -= 13;
            }
            sb.append( c );
        }
        return sb.toString();
    }
}