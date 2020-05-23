package it.unina.sistemiembedded.utility.ui;

import it.unina.sistemiembedded.boundary.dialog.LongRunningDialog;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UILongRunningHelper {

    private static final String DEFAULT_MESSAGE = "Operation in progress, please wait...";

    /**
     * Display a dialog with default message to handle long running tasks, by running them async
     * @param parent JFrame parent to center the dialog to
     * @param runnable Runnable long running code to execute async
     */
    public static void runAsync(JFrame parent, Runnable runnable) {

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
    public static void runAsync(JFrame parent, String message, Runnable runnable) {

        LongRunningDialog longRunningDialog = new LongRunningDialog(message, parent);
        longRunningDialog.setVisible(true);
        CompletableFuture.runAsync(runnable).thenRun(longRunningDialog::dispose);

    }


    /**
     * Display a dialog with a custom message to handle long running tasks, by running them async
     * @param parent JFrame parent to center the dialog to
     * @param message String message to be displayed while the operation is in progress
     * @param supplier Runnable promise resolve code
     * @param consumer Consumer code to execute when promise resolves
     * @param <T> type supplied by supplier and consumed by consumer then
     */
    public static<T> void supplyAsync(JFrame parent, String message, Supplier<T> supplier, Consumer<T> consumer) {

        LongRunningDialog longRunningDialog = new LongRunningDialog(message, parent);
        longRunningDialog.setVisible(true);
        CompletableFuture.<T>supplyAsync(supplier).thenAccept(consumer).thenRun(longRunningDialog::dispose);

    }

}
