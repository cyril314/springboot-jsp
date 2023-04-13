package com.fit.common.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@WebServlet(urlPatterns = "/assets/img/securityCode.jpg")
public class SecurityCodeServlet extends HttpServlet {

    private static final long serialVersionUID = -770474964506858977L;

    public static final String CODE = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ";
    public static final int CODE_LENGTH = CODE.length();
    private static final Logger log = LoggerFactory.getLogger(SecurityCodeServlet.class);

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/jpeg");
        this.createImage(request, response);
    }

    private void createImage(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0L);
        byte width = 60;
        byte height = 30;
        BufferedImage image = new BufferedImage(width, height, 1);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Serif", 0, 18));
        g.setColor(this.getRandColor(160, 200));

        int e;
        int code;
        for (int sRand = 0; sRand < 155; ++sRand) {
            e = random.nextInt(width);
            code = random.nextInt(height);
            int rand = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(e, code, e + rand, code + yl);
        }

        String var14 = "";

        for (e = 0; e < 4; ++e) {
            code = random.nextInt(CODE_LENGTH);
            String var15 = String.valueOf(CODE.charAt(code));
            var14 = var14.concat(var15);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(var15, 13 * e + 6, 16);
        }

        request.getSession().setAttribute("KAPTCHA_SESSION_KEY", var14.toLowerCase());
        g.dispose();

        try {
            ImageIO.write(image, "JPEG", response.getOutputStream());
        } catch (IOException var13) {
            this.log.error("SecurityCodeSevlet.createImage() : Failed : " + var13.getMessage());
        }

    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }

        if (bc > 255) {
            bc = 255;
        }

        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
