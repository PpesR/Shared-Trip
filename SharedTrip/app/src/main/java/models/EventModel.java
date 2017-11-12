package models;


public class EventModel {

    protected int id;
    protected String name;
    protected String imageLink;
    protected String loc;
    protected String startDate;
    protected String endDate;
    protected String description;
    protected int spots;
    protected int cost;

    public EventModel() { super(); }

    public EventModel(String name, String imageLink, String loc) {
        this.name = name;
        this.imageLink = imageLink;
        this.loc = loc;
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }

    public String getLoc() { return loc; }
    public void setLoc(String loc) { this.loc = loc; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSpots() { return spots; }
    public void setSpots(int spots) { this.spots = spots; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
}