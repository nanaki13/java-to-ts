package bon.jo;

import java.util.List;

public class UserJava {

    private String id;
    private List<UserJava> users;
    private UserJava pere;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<UserJava> getUsers() {
        return users;
    }

    public void setUsers(List<UserJava> users) {
        this.users = users;
    }

    public UserJava getPere() {
        return pere;
    }

    public void setPere(UserJava pere) {
        this.pere = pere;
    }
}
