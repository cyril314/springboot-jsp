package com.fit.web;

import com.fit.common.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping({"system/fileUpload"})
public class FileUploadController extends BaseController {

    @RequestMapping(value = {"i/fileUpload"}, method = {RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> fileUpload(MultipartFile fileToUpload, HttpServletRequest request) throws Exception {
        String filename = fileToUpload.getOriginalFilename();
        System.out.println("上传文件:" + filename);
        String realPathStr = request.getSession().getServletContext().getRealPath("/backup");
        File realPath = new File(realPathStr);
        if (!realPath.exists()) {
            realPath.mkdirs();
        }

        File writeFile = new File(realPath + File.separator + filename);
        HashMap map = new HashMap();
        String mess = "";
        String status = "";

        try {
            FileCopyUtils.copy(fileToUpload.getBytes(), writeFile);
            mess = "文件上传完成！";
            status = "success";
        } catch (Exception var11) {
            System.out.println(var11.getMessage());
            mess = var11.getMessage();
            status = "fail";
        }

        map.put("mess", mess);
        map.put("status", status);
        return map;
    }
}