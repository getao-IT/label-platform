package cn.iecas.geoai.labelplatform.service.impl;

import cn.iecas.geoai.labelplatform.dao.LabelCategoryMapper;
import cn.iecas.geoai.labelplatform.entity.domain.LabelCategory;
import cn.iecas.geoai.labelplatform.service.LabelCategoryService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabelCategoryServiceImpl extends ServiceImpl<LabelCategoryMapper, LabelCategory> implements LabelCategoryService {

    @Autowired
    LabelCategoryMapper labelCategoryMapper;

}
