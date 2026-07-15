package com.admin.base.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DestructiveTestDatabaseGuard {

    private static final Pattern MYSQL_DATABASE = Pattern.compile("^jdbc:mysql://[^/]+/([^?;]+)(?:[?;].*)?$");

    private DestructiveTestDatabaseGuard() {
    }

    public static void requireIsolatedDatabase(String jdbcUrl) {
        Matcher matcher = MYSQL_DATABASE.matcher(jdbcUrl == null ? "" : jdbcUrl);
        if (!matcher.matches()) {
            throw new IllegalStateException("Destructive tests require a valid MySQL JDBC URL");
        }
        if (!matcher.group(1).endsWith("_it")) {
            throw new IllegalStateException("Destructive tests require a database name ending with _it");
        }
    }
}
