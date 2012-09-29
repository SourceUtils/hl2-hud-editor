/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timepath.tf2;

import java.io.File;

/**
 *
 * @author andrew
 */
class MyTreeObject {
    
    public final Object obj;

    public MyTreeObject(Object obj) {
        this.obj = obj;
    }
    
    @Override
    public String toString() {
        if(obj instanceof File) {
            return ((File) obj).getName();
        }
        return obj.getClass() + ":" + (String) obj;
    }
    
}
