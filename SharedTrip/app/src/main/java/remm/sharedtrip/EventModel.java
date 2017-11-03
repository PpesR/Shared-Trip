package remm.sharedtrip;


public class EventModel {

    private int id;
    private String name;
    private String imageLink;
    private String loc;

    public EventModel(String name, String imageLink, String loc) {
        this.name = name;
        this.imageLink = imageLink;
        this.loc = loc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

}