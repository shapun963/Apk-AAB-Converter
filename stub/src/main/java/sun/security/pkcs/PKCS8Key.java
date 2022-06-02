package sun.security.pkcs;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import sun.security.util.DerValue;

@SuppressWarnings("all")
public class PKCS8Key implements PrivateKey {

    public void decode(InputStream is) throws InvalidKeyException{
        throw new RuntimeException("Stub!");
    }

    public void decode(byte[] bytes) throws InvalidKeyException{
        throw new RuntimeException("Stub!");
    }
    
    //public default void javax.security.auth.Destroyable.destroy() throws javax.security.auth.DestroyFailedException
    public byte[] encode() throws InvalidKeyException{
        throw new RuntimeException("Stub!");
    }
    //public final void encode(sun.security.util.DerOutputStream) throws java.io.IOException
    //public boolean sun.security.pkcs.PKCS8Key.equals(java.lang.Object)
    @Override
    public String getAlgorithm(){
        throw new RuntimeException("Stub!");
    }
    //public sun.security.x509.AlgorithmId sun.security.pkcs.PKCS8Key.getAlgorithmId()
    //public final java.lang.Class java.lang.Object.getClass()
    public synchronized byte[] getEncoded(){
        throw new RuntimeException("Stub!");
    }
    public String getFormat(){
        throw new RuntimeException("Stub!");
    }
    //public int sun.security.pkcs.PKCS8Key.hashCode()
    //public default boolean javax.security.auth.Destroyable.isDestroyed()
    //public final native void java.lang.Object.notify()
    //public final native void java.lang.Object.notifyAll()
    public static PKCS8Key parse(DerValue derValue) throws IOException {
        throw new RuntimeException("Stub!");
    }
    public static java.security.PrivateKey parseKey(DerValue derValue) throws IOException{
        throw new RuntimeException("Stub!");
    }
    //public java.lang.String java.lang.Object.toString()
    //public final native void java.lang.Object.wait() throws java.lang.InterruptedException
    //public final void java.lang.Object.wait(long) throws java.lang.InterruptedException
    //public final native void java.lang.Object.wait(long,int) throws java.lang.InterruptedException
}
