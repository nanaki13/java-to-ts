package bon.jo;

import java.util.Date;
import java.util.List;

public class Group {

    private String name;
    private Date creationDate;
    private List<UserJava> users;

    public List<UserJava> getUsers() {
        return users;
    }

    public void setUsers(List<UserJava> users) {
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
