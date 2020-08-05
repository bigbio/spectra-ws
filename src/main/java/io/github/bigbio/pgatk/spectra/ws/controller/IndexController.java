package io.github.bigbio.pgatk.spectra.ws.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class IndexController {

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Value("${springdoc.api-docs.path}")
    private String apiDocsPath;

    @RequestMapping(method = RequestMethod.GET, path = {"/"})
    public String getSwaggerUI(HttpServletRequest request) {
        String url = request.getRequestURL().toString();

        log.debug("redirect for " + url + " => swagger-ui page");
        //for "/" URL, we redirect client to actual swagger page
        String apiDocsFullpath = contextPath + apiDocsPath;
        return "redirect:swagger-ui/index.html?url=" + apiDocsFullpath + "&configUrl=" + apiDocsFullpath + "/swagger-config";
    }

}
