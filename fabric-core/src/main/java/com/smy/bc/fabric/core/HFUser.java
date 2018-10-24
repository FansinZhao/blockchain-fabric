package com.smy.bc.fabric.core;

import lombok.Data;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.Serializable;
import java.util.Set;

/**
 * @program: HFUser
 * @description: 实现User接口
 * @author: Zhun.Xiao
 * @create: 2018-05-13 11:11
 **/
@Data
public class HFUser implements User, Serializable {


    private String name;
    private String account;
    private String affiliation;
    private String mspId;
    private Set<String> roles;
    private Enrollment enrollment;
    private String passwd;

    public HFUser(String name, String affiliation, String mspId, Enrollment enrollment) {
        this.name = name;
        this.affiliation = affiliation;
        this.mspId = mspId;
        this.enrollment = enrollment;
    }


}
