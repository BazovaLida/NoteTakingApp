package iv81.dtbl.noteapp.models;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Arrays;

public class File {

    @Id
    private String id;

    private String title;

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    private String authorId;
    private ArrayList<String> usersIds;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    private boolean isPublic;

    private String body;
    private String parentId;
    private ArrayList<File> children;

    public File() {}
    public File(String Ntitle, String auth, String NpId) {
        this.title = Ntitle;
        this.isPublic = false;
        this.authorId = auth;
        this.usersIds = new ArrayList<>();
        this.body = "";
        this.parentId = NpId;
    }

    @Override
    public String toString() { return String.format("User [id: %s, title: %s]", id, title); }

    public String getId() {return this.id;}
    public String getTitle() {return this.title;}
    public ArrayList<String> getUsersIds() {return this.usersIds;}
    public boolean isPublic() {return this.isPublic;}
    public void setPublic(boolean isPublic) { this.isPublic = isPublic;}

    public void setTitle(String Ntitle) {this.title = Ntitle;}
    public void addUser(String Nuid) {this.usersIds.add(Nuid);}
    public void addUsers(ArrayList<String> Nuids) {this.usersIds.addAll(Nuids);}
    public void removeUser(String uid) {this.usersIds.remove(uid);}
    public void removeUsers(ArrayList<String> uids) {this.usersIds.removeAll(uids);}


}
