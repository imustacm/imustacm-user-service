package cn.imustacm.user.controller;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.QiniuDTO;
import cn.imustacm.user.model.Option;
import cn.imustacm.user.service.OptionService;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 七牛
 *
 * @author liandong
 * @date 2019/08/18
 */
@RestController
@RequestMapping("/qiniu")
public class QiniuController {

    @Autowired
    private OptionService optionService;

    /**
     * 获取七牛云Token
     *
     * @return
     */
    @GetMapping("/token")
    public Resp getQiniuToken() {
        Option option = optionService.getByKey("qiniu");
        JSONObject value = JSONObject.parseObject(option.getValue());
        String accessKey = value.getString("accesskey");
        String secretKey = value.getString("secretkey");
        String bucket = value.getString("bucket");
        Auth auth = Auth.create(accessKey, secretKey);
        String token = auth.uploadToken(bucket);
        return Resp.ok(QiniuDTO.builder().token(token).build());
    }
}
