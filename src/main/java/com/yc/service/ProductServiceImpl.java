package com.yc.service;

import com.yc.dao.ProductDao;
import com.yc.springframework.annotations.YcResource;
import com.yc.springframework.annotations.YcService;


@YcService(value = "psi")
public class ProductServiceImpl implements ProductService {

    private ProductDao productDao;

    @YcResource(name="productDaoImpl")
    public void setProductDao(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public void select() {
        productDao.select();
    }

    @Override
    public void add() {
        productDao.add();
    }

    @Override
    public void del() {
        productDao.del();
    }
}
