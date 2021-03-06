package server.model;

import server.auxilary.AccessLevel;
import server.auxilary.IO;

/**
 * Created by th3gh0st on 2017/12/23.
 * @author th3gh0st
 */
public class Vericode extends ApplicationObject
{
    private String code_name;
    private String code;

    public Vericode(String code_name, String code)
    {
        setCode_name(code_name);
        setCode(code);
    }

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    public String getCode_name()
    {
        return code_name;
    }

    public void setCode_name(String code_name)
    {
        this.code_name = code_name;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public String[] isValid()
    {
        if(getCode_name()==null)
            return new String[]{"false", "invalid code_name value."};
        if(getCode()==null)
            return new String[]{"false", "invalid code value."};

        return super.isValid();
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        switch (var.toLowerCase())
        {
            case "code_name":
                code_name = (String)val;
                break;
            case "code":
                code=(String)val;
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'");
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        Object val = super.get(var);
        if(val==null)
        {
            switch (var.toLowerCase())
            {
                case "code_name":
                    return code_name;
                case "code":
                    return code;
                default:
                    return null;
            }
        } else return val;
    }

    @Override
    public String toString()
    {
        return super.toString() + " codename "  + getCode_name() + " code " +getCode();
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/vericode";
    }
}