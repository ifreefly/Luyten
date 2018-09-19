package idevcod.windows;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class AppIdRegisterUtil {
    public static void setCurrentProcessExplicitAppUserModelID(final String appID)
    {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
            throw new RuntimeException("unable to set current process explicit AppUserModelID to: " + appID);
    }

    public static String getCurrentProcessExplicitAppUserModelID()
    {
        String appUserModelId = "N/A";
        final PointerByReference r = new PointerByReference();

        if (GetCurrentProcessExplicitAppUserModelID(r).longValue() == 0)
        {
            final Pointer p = r.getValue();


            appUserModelId =  p.getWideString(0); // here we leak native memory by lazyness
            Ole32.INSTANCE.CoTaskMemFree(p);
        }
        return appUserModelId;
    }

    private static native NativeLong GetCurrentProcessExplicitAppUserModelID(PointerByReference appID);
    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    static
    {
        Native.register("shell32");
    }

    private interface Ole32 extends StdCallLibrary {

        Ole32 INSTANCE = (Ole32) Native.loadLibrary(
                "Ole32", Ole32.class, W32APIOptions.UNICODE_OPTIONS);

        void CoTaskMemFree(Pointer pv);
    }
}
