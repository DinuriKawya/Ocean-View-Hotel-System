package oceanview.factory;

import oceanview.model.Room;


public abstract class AbstractRoom extends Room {

    public AbstractRoom() {
        configureRoom();   
    }

    protected abstract void configureRoom();
}