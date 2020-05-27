package it.unina.sistemiembedded.driver;

public class OutputBuffer {

    private boolean busy = false;

    public OutputBuffer() {
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

}
