package com.pustakey.pustakey.viewobject.holder;

import com.pustakey.pustakey.utils.Constants;

public class CategoryParameterHolder {

    public String order_by, cityId;

    public CategoryParameterHolder() {

        this.order_by = Constants.FILTERING_ADDED_DATE;
        this.cityId = "";

    }

    public CategoryParameterHolder getTrendingCategories()
    {
        this.cityId = "";
        this.order_by = Constants.FILTERING_TRENDING;

        return this;
    }

    public String changeToMapValue() {
        String result = "";

        if (!cityId.isEmpty()) {
            result += cityId;
        }

        if (!order_by.isEmpty()) {
            result += order_by;
        }

        return result;
    }
}
