package edu.jiangxin.apktoolbox.file.password.recovery.checker;

import com.github.junrar.Archive;
import com.github.junrar.exception.CrcErrorException;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarVersionException;
import com.github.junrar.exception.WrongPasswordException;
import com.github.junrar.rarfile.FileHeader;
import edu.jiangxin.apktoolbox.file.password.recovery.exception.UnknownException;
import edu.jiangxin.apktoolbox.file.password.recovery.exception.UnsupportedVersionException;

import java.io.IOException;
import java.io.OutputStream;

public final class RarChecker extends FileChecker {
    private static final boolean DEBUG = false;

    public RarChecker() {
        super();
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"rar"};
    }

    @Override
    public String getFileDescription() {
        return "*.rar";
    }

    @Override
    public String getDescription() {
        return "RAR Checker(RAR1.4–7)";
    }

    @Override
    public boolean prepareChecker() {
        return true;
    }

    @Override
    public boolean checkPassword(String password) {
        boolean result = false;
        try (Archive archive = new Archive(file, password)) {
            while (true) {
                FileHeader fileHeader = archive.nextFileHeader();
                if (fileHeader == null) {
                    break;
                }
                archive.extractFile(fileHeader, new OutputStream() {
                    @Override
                    public void write(int b) {
                    }
                });
            }
            result = true;
        } catch (CrcErrorException e) {
            if (DEBUG) {
                logger.error("[CrcErrorException]password is incorrect: {}", password);
            }
        } catch (UnsupportedRarVersionException e) {
            throw new UnsupportedVersionException(e);
        } catch (WrongPasswordException e) {
            if (DEBUG) {
                logger.error("[WrongPasswordException]password is incorrect: {}", password);
            }
        } catch (RarException e) {
            if (DEBUG) {
                logger.error("[RarException]password is incorrect: {}", password);
            }
        } catch (IOException e) {
            throw new UnknownException(e);
        }
        return result;
    }
}
