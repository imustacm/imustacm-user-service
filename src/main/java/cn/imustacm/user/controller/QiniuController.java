package cn.imustacm.user.controller;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.QiniuDTO;
import com.qiniu.util.Auth;
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

    @Value("${qiniu.accessKey}")
    private String accessKey;
    @Value("${qiniu.secretKey}")
    private String secretKey;
    @Value("${qiniu.bucket}")
    private String bucket;

    /**
     * 获取七牛云Token
     *
     * @return
     */
    @GetMapping("/token")
    public Resp getQiniuToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        String token = auth.uploadToken(bucket);
        return Resp.ok(QiniuDTO.builder().token(token).build());
    }
}
