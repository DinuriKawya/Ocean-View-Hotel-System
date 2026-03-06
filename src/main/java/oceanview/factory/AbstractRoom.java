package oceanview.factory;

import oceanview.model.Room;

public abstract class AbstractRoom {

    protected Room room;  

    public AbstractRoom() {
        this.room = new Room();
        configureRoom();
    }

    protected abstract void configureRoom();
}