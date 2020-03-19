package android.dbmeter.net.model;

public class Category {
    private int CategoryId;
    private int CategoryThumbnail;
    private String CategoryTitle;

    public Category(){
        CategoryId = 0;
        CategoryThumbnail = 0;
        CategoryTitle = "";
    }

    public Category(int categoryId, int categoryThumbnail, String categoryTitle){
        CategoryId = categoryId;
        CategoryThumbnail = categoryThumbnail;
        CategoryTitle = categoryTitle;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public int getCategoryThumbnail() {
        return CategoryThumbnail;
    }

    public void setCategoryThumbnail(int categoryThumbnail) {
        CategoryThumbnail = categoryThumbnail;
    }

    public String getCategoryTitle() {
        return CategoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        CategoryTitle = categoryTitle;
    }
}
