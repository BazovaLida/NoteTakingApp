package iv81.dtbl.noteapp.models;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Arrays;

public class File {

    @Id
    private String id;

    private String title;
    private String authorId;
    private ArrayList<String> usersIds;
    private ArrayList<String> elements;
    private String parentId;
    private ArrayList<File> children;

    public File() {}
    public File(String Ntitle, String auth, String NpId) {
        this.title = Ntitle;
        this.authorId = auth;
        this.usersIds = new ArrayList<>();
        this.usersIds.add(auth);
        this.elements = new ArrayList<>();
        this.parentId = NpId;
    }

    @Override
    public String toString() { return String.format("User [id: %s, title: %s]"); }

    public String getId() {return this.id;}
    public String getTitle() {return this.title;}
    public ArrayList<String> getUsersIds() {return this.usersIds;}
    public ArrayList<String> getElements() {return this.elements;}

    public void setTitle(String Ntitle) {this.title = Ntitle;}
    public void addUser(String Nuid) {this.usersIds.add(Nuid);}
    public void addUsers(ArrayList<String> Nuids) {this.usersIds.addAll(Nuids);}
    public void removeUser(String uid) {this.usersIds.remove(uid);}
    public void removeUsers(ArrayList<String> uids) {this.usersIds.removeAll(uids);}
    public void addElement(String Nelem) {this.elements.add(Nelem);}
    public void addElements(ArrayList<String> Nelems) { this.elements.addAll(Nelems);}
    public void insertElement(int ind, String Nelem) { this.elements.add(ind, Nelem);}


}
