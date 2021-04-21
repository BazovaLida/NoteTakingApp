package iv81.dtbl.noteapp.util;

public class FileElement {
    
    private ElementType type = ElementType.PLAINTEXT;
    private String bgColor = "#FFFFFF";
    private String textColor = "#000000";
    private String text = "";
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean crossline = false;
    
    public FileElement() { }
    public FileElement(ElementType elType) {this.type = elType;}
    
    public void setText(String Ntext) { this.text = Ntext; }
    public void setType(ElementType Ntype) {this.type = Ntype;}
    public void setBgColor(String Ncolor) {this.bgColor = Ncolor;}
    public void setTextColor(String Ncolor) {this.textColor = Ncolor;}
    
    public void toggleBold() {this.bold = !this.bold;}
    public void toggleItalic() {this.italic = !this.italic;}
    public void toggleUnderline() {this.underline = !this.underline;}
    public void toggleCrossLine() {this.crossline = !this.crossline;}
    
    public String toHTML() {
        String res = "";
        //generate HTML here
        return res;
    }
    
    public String toMD() {
        String res = "";
        //generate Markdown here
        return res;
    }
}


