package server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LockFile implements Comparable<LockFile> {
    public static final String LOCK_FILE_TYPE = ".lock";
    private String serverFileName;
    private int lockClientIndex;

    public LockFile(String lockFileName) {
        this.serverFileName = lockFileName.substring(0, lockFileName.indexOf('_'));
        this.lockClientIndex = Integer.parseInt(lockFileName.split("_")[1]);
    }

    public String getLockFileName() {
        return serverFileName + "_" + lockClientIndex + "_" + LOCK_FILE_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockFile lockFile = (LockFile) o;
        return lockClientIndex == lockFile.lockClientIndex && serverFileName.equals(lockFile.serverFileName);
    }

    @Override
    public int compareTo(LockFile o) {
        return Integer.compare(lockClientIndex, o.lockClientIndex);
    }

    @Override
    public String toString() {
        return getLockFileName();
    }

    public static boolean isLockFile(String fileName) {
        return fileName.endsWith(LOCK_FILE_TYPE);
    }

    public void decrementClientLockIndex() {
        lockClientIndex--;
    }
}
