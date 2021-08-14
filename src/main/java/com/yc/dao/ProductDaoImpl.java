package com.yc.dao;


import com.yc.springframework.annotations.YcRepository;

@YcRepository
public class ProductDaoImpl implements ProductDao {
    @Override
    public void select() {
        System.out.println("ProductDaoImpl() select");
    }

    @Override
    public void add() {
        System.out.println("ProductDaoImpl() add");
    }

    @Override
    public void del() {
        System.out.println("ProductDaoImpl() del");
    }
}
