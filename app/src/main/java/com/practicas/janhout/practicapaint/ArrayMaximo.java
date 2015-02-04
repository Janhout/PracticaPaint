package com.practicas.janhout.practicapaint;

import java.util.ArrayList;

public class ArrayMaximo<Object> extends ArrayList<Object> {

    private int max;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public ArrayMaximo(int max){
        super();
        this.max = max;
    }

    @Override
    public boolean add(Object object) {
        if(super.size()<max){
            return super.add(object);
        } else{
            super.remove(0);
            super.trimToSize();
            return super.add(object);
        }
    }

    public Object sacar() {
        Object c = super.get(this.size()-1);
        super.remove(super.size()-1);
        super.trimToSize();
        return c;
    }
}
