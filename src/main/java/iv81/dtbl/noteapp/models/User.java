package iv81.dtbl.noteapp.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class User  {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String passHash;
    private boolean enabled;
    
    private String name;
    private String bio;

    public User() {
        super();
        this.enabled=false;
    }

    public User(String uname, String Nemail, String NpassHash) {
        this.name = uname;
        this.email = Nemail;
        this.passHash = NpassHash;
    }

    @Override
    public String toString() {
        return String.format("User [id: %s, name: %s, e-mail: %s]", id, name, email);
    }
    
    public void setName(String Nname) {this.name = Nname;}
    public void setEmail(String Nemail) {this.email = Nemail;}
    public void setPassHash(String NpassHash) {this.passHash = NpassHash;}
    public void setBio(String Nbio) {this.bio = Nbio;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}
    
    public String getId() {return this.id;}
    public String getEmail() {return this.email;}
    public String getPassHash() {return this.passHash;}
    public String getName() {return this.name;}
    public String getBio() {return this.bio;}
    public boolean isEnabled() {return this.enabled;}

}
