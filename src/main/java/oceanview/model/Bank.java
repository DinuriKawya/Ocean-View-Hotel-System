package oceanview.model;

public class Bank {

    private int     bankId;
    private String  name;
    private boolean active;

    public Bank() {}

    public Bank(int bankId, String name, boolean active) {
        this.bankId = bankId;
        this.name   = name;
        this.active = active;
    }

    public int     getBankId()  { return bankId; }
    public String  getName()    { return name; }
    public boolean isActive()   { return active; }

    public void setBankId(int bankId)    { this.bankId = bankId; }
    public void setName(String name)     { this.name   = name; }
    public void setActive(boolean active){ this.active  = active; }

    @Override
    public String toString() { return name; }
}
