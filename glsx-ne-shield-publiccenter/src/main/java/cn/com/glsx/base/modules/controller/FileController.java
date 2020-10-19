package cn.com.glsx.base.modules.controller;

import com.glsx.plat.common.utils.SnowFlake;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.context.properties.UploadProperties;
import com.glsx.plat.core.web.R;
import com.glsx.plat.fastdfs.FastDfsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author payu
 */
@Slf4j
@RestController
@RequestMapping(value = "basic/file")
public class FileController {

    @Resource
    private FastDfsUtils fastDfsUtils;

    @Resource
    private UploadProperties uploadProperties;

    /**
     * 文件上传到本地服务器
     */
    @PostMapping(value = "/upload2local", headers = "content-type=multipart/form-data")
    public R upload2local(@RequestParam("file") MultipartFile file) throws Exception {
        String result;

        //获取文件存放路径
        String path = uploadProperties.getBasePath();

        File targetDir = new File(path);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.indexOf("."));
        String newFilename = SnowFlake.nextId() + suffix;

        String tempPath = targetDir + File.separator + newFilename;

        // 转存文件
        File desc = new File(tempPath);
        FileUtils.copyInputStreamToFile(file.getInputStream(), desc);

        path = tempPath;
        if (StringUtils.isNotEmpty(path)) {
            result = uploadProperties.getFilepath() + newFilename;
            return R.ok().data(result);
        } else {
            result = "上传失败";
            return R.error(result);
        }
    }

    /**
     * 文件上传分布式文件服务器
     */
    @PostMapping(value = "/upload2dfs", headers = "content-type=multipart/form-data")
    public R upload2dfs(@RequestParam("file") MultipartFile file) {
        String result;
        try {
            String path = fastDfsUtils.upload(file);
            if (StringUtils.isNotEmpty(path)) {
                result = path;
            } else {
                result = "上传失败";
            }
            return R.ok().data(result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "文件服务异常";
            return R.error(result);
        }
    }

    /**
     * 文件删除
     *
     * @param url
     * @return
     */
    @GetMapping(value = "/deleteByUrl")
    public R deleteByUrl(@RequestParam("url") String url) {
        fastDfsUtils.delete(url);
        return R.ok();
    }

}
