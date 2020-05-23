package it.unina.sistemiembedded.utility.ui;

import it.unina.sistemiembedded.boundary.dialog.LongRunningDialog;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;

public class UILongRunningHelper {

    private static final String DEFAULT_MESSAGE = "Operation in progress, please wait...";

    /**
     * Display a dialog with default message to handle long running tasks, by running them async
     * @param parent JFrame parent to center the dialog to
     * @param runnable Runnable long running code to execute async
     */
    public static void runAsync(JFrame parent, Runnable runnable) throws InterruptedException {

        LongRunningDialog longRunningDialog = new LongRunningDialog(DEFAULT_MESSAGE, parent);
        longRunningDialog.setVisible(true);
        CompletableFuture.runAsync(runnable).thenRun(longRunningDialog::dispose);

    }

    /**
     * Display a dialog with a custom message to handle long running tasks, by running them async
     * @param parent JFrame parent to center the dialog to
     * @param message String message to be displayed while the operation is in progress
     * @param runnable Runnable long running code to execute async
     */
    public static void runAsync(JFrame parent, String message, Runnable runnable) throws InterruptedException {

        LongRunningDialog longRunningDialog = new LongRunningDialog(message, parent);
        longRunningDialog.setVisible(true);
        CompletableFuture.runAsync(runnable).thenRun(longRunningDialog::dispose);

    }


}
