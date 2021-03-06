package server.model;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import server.auxilary.AccessLevel;
import server.auxilary.Globals;
import server.auxilary.IO;

/**
 * Created by th3gh0st on 2017/12/23.
 * @author th3gh0st
 */
public abstract class PurchaseOrderItem extends ApplicationObject
{
    private int item_number;
    private String purchase_order_id;
    private String item_id;
    private int quantity;
    private double discount;
    private double cost;

    public PurchaseOrderItem()
    {}

    public PurchaseOrderItem(String _id)
    {
        super(_id);
    }

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.ADMIN;
    }

    public int getItem_number()
    {
        return item_number;
    }

    public void setItem_number(int item_number)
    {
        this.item_number = item_number;
    }

    public String getPurchase_order_id()
    {
        return purchase_order_id;
    }

    public void setPurchase_order_id(String purchase_order_id)
    {
        this.purchase_order_id = purchase_order_id;
    }

    public String getItem_id()
    {
        return item_id;
    }

    public void setItem_id(String item_id)
    {
        this.item_id = item_id;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public double getCost()
    {
        return cost;
    }

    public void setCost(double cost)
    {
        this.cost=cost;
    }

    public double getDiscount(){return this.discount;}

    public void setDiscount(double discount)
    {
        this.discount = discount;
    }

    public Resource getItem()
    {
        return IO.getInstance().mongoOperations().findOne(new Query(Criteria.where("_id").is(getItem_id())), Resource.class, "resources");
    }

    public String getUnit()
    {
        Resource res = IO.getInstance().mongoOperations().findOne(new Query(Criteria.where("_id").is(getItem_id())), Resource.class, "resources");
        if(res!=null)
            return res.getUnit();
        return "N/A";
    }

    public String getItem_description()
    {
        Resource res = IO.getInstance().mongoOperations().findOne(new Query(Criteria.where("_id").is(getItem_id())), Resource.class, "resources");
        if(res!=null)
            return res.getResource_description();
        return "N/A";
    }

    public double getRate()
    {
        return getCost() - (getCost() * getDiscount()/100);
    }

    public String getTotal()
    {
        double total = getRate()*getQuantity();
        return Globals.CURRENCY_SYMBOL.getValue() + " " + total;
    }

    @Override
    public String[] isValid()
    {
        if(getItem_id()==null)
            return new String[]{"false", "invalid item_id value."};
        if(getItem_number()<0)
            return new String[]{"false", "invalid item_number value."};
        if(getPurchase_order_id()==null)
            return new String[]{"false", "invalid purchase_order_id value."};
        if(getCost()<0)
            return new String[]{"false", "invalid cost value."};
        if(getDiscount()<0)
            return new String[]{"false", "invalid discount value."};
        if(getQuantity()<=0)
            return new String[]{"false", "invalid quantity value."};

        return super.isValid();
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "purchase_order_id":
                    setPurchase_order_id(String.valueOf(val));
                    break;
                case "item_id":
                    setItem_id(String.valueOf(val));
                    break;
                case "item_number":
                    setItem_number(Integer.valueOf((String)val));
                    break;
                case "quantity":
                    setQuantity(Integer.valueOf((String)val));
                    break;
                case "discount":
                    setDiscount(Double.parseDouble((String) val));
                    break;
                case "cost":
                    setCost(Double.parseDouble((String) val));
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown "+getClass().getName()+" attribute '" + var + "'.");
                    break;
            }
        }catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "purchase_order_id":
                return getPurchase_order_id();
            case "item_id":
                return getItem_id();
            case "item_number":
                return getItem_number();
            case "cost":
                return getCost();
            case "quantity":
                return getQuantity();
            case "discount":
                return getDiscount();
        }
        return super.get(var);
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String toString()
    {
        return super.toString() + " for PO ["  + getPurchase_order_id() + "] item [" +getItem_id() + "]";
    }
}