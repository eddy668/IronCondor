package com.nanobytes.ironcondor;

import android.util.Log;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Objects;

public class RetryTest implements TestRule {
    private int retryCount;

    public RetryTest(int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                for (int i = 0; i < retryCount; i++)
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        Log.e("IronCondorTester", description.getDisplayName() + ": run " + (i + 1) + " failed");
                    }
                Log.e("IronCondorTester", description.getDisplayName() + ": giving up after " + retryCount + " failures");
                throw Objects.requireNonNull(caughtThrowable);
            }
        };
    }
}