package me.simplyran.simplymines.updater;

public record UpdateCheckResult(String currentVersion, String latestVersion, boolean updateAvailable, boolean checkFailed) {
}
