package in.goods24.pojo;

/**
 * Created by Shikhar on 11/15/2017.
 */

public class ProductPOJO {
    private String userID;
    private String productID;
    private String productName;
    private String productDesc;
    private String productQuantity;
    private String productRate;
    private String productDiscount;
    private String productImage;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductRate() {
        return productRate;
    }

    public void setProductRate(String productRate) {
        this.productRate = productRate;
    }

    public String getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(String productDiscount) {
        this.productDiscount = productDiscount;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    @Override
    public String toString() {
        return "ProductPOJO{" +
                "userID='" + userID + '\'' +
                ", productID='" + productID + '\'' +
                ", productName='" + productName + '\'' +
                ", productDesc='" + productDesc + '\'' +
                ", productQuantity='" + productQuantity + '\'' +
                ", productRate='" + productRate + '\'' +
                ", productDiscount='" + productDiscount + '\'' +
                ", productImage='" + productImage + '\'' +
                '}';
    }
}
