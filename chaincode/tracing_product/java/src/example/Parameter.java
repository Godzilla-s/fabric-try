package example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.HashMap;

public class Parameter {
    private String mspID;
    private String function;
    private JSONObject args;
    private HashMap<String, String> ownership;
    private Asset asset;

    Parameter(String funcName, Asset asset, String mspID, HashMap<String,String> ownership, JSONObject args) {
        this.function = funcName;
        this.mspID = mspID;
        this.asset = asset;
        this.args = args;
        this.ownership = ownership;
    }

    private String authKey(String authID) {
        return "authority-" + asset.className() + "-" + authID;
    }

    public String getFunction() {
        return function;
    }

    public JSONObject getArgs() {
        return args;
    }

    private boolean isReadable(String rw) {
        return (rw.equals("r") || rw.equals("readOnly") || rw.equals("ro") || rw.equals("readWrite") || rw.equals("rw"));
    }

    private boolean isWritable(String rw) {
        return (rw.equals("w") || rw.equals("writeOnly") || rw.equals("wo") || rw.equals("readWrite") || rw.equals("rw"));
    }

    // 是否为所属的读权限
    public boolean isOwnershipReadable(ChaincodeStub stub) {
        String rw = this.ownership.get(this.mspID);
        return isReadable(rw);
    }

    // 是否为所属的写权限
    public  boolean isOwnershipWritable(ChaincodeStub stub) {
        String rw = this.ownership.get(this.mspID);
        return isWritable(rw);
    }

    // 是否为授权的读权限
    public boolean isAuthReadable(ChaincodeStub stub) {
        String key = authKey(this.mspID);
        byte[] authData = stub.getState(key);
        if (authData.length > 0) {
            return isReadable(new String(authData));
        }
        return false;
    }

    // 是否为授权的写权限
    public boolean isAuthWritable(ChaincodeStub stub) {
        String key = this.authKey(this.mspID);
        byte[] authData = stub.getState(key);
        if (authData.length > 0) {
            return isWritable(new String(authData));
        }
        return false;
    }

    // 设置权限
    public int setAuth(ChaincodeStub stub, String mspID, String rw) {
        String key = authKey(mspID);
        byte[] authData = stub.getState(key);
        if (authData.length > 0) {
            return 1;
        }
        stub.putState(key, rw.getBytes());
        return 0;
    }

    public void unsetAuth(ChaincodeStub stub, String mspID) {
        String key = authKey(mspID);
        byte[] authData = stub.getState(key);
        if (authData.length > 0) {
            stub.delState(key);
            return ;
        }
        return ;
    }

    public String getString(String key) {
        return this.args.getString(key);
    }

    public int getInt(String key) {
        return this.args.getIntValue(key);
    }

    public JSONObject getJSONObject(String key) {
        return this.args.getJSONObject(key);
    }

    public Object parseObject(String className) throws Exception {
        try {
            Class<?> cls = ClassLoader.getSystemClassLoader().loadClass(className);
            String jsonStr = args.toJSONString();
            return JSON.parseObject(jsonStr, cls);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public String getJSONObjectString() {
        return args.toJSONString();
    }
}

