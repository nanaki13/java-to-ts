package bon.jo;

import java.util.List;

public class UserJava {

    private String id;
    private List<UserJava> users;
    private List<Long> points;
    private UserJava pere;
    private Sex sex;

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    private int truc;

    public int getTruc() {
        return truc;
    }

    public void setTruc(int truc) {
        this.truc = truc;
    }

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

    public List<Long> getPoints() {
        return points;
    }

    public void setPoints(List<Long> points) {
        this.points = points;
    }
}
