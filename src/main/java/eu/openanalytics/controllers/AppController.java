/**
 * ShinyProxy
 *
 * Copyright (C) 2016-2017 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.controllers;

import eu.openanalytics.ShinyProxyApplication;
import eu.openanalytics.services.AppService;
import eu.openanalytics.services.DockerService;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AppController extends BaseController {

	@Inject
	DockerService dockerService;
	
	@Inject
	AppService appService;

	@RequestMapping(value="/app/*", method=RequestMethod.GET)
	String app(ModelMap map, HttpServletRequest request) {
		prepareMap(map, request);
		
		String mapping = dockerService.getMapping(request, getUserName(request), getAppName(request), false);
		String contextPath = ShinyProxyApplication.getContextPath(environment);

		map.put("appTitle", getAppTitle(request));
		map.put("container", buildContainerPath(mapping, request));
		map.put("heartbeatRate", environment.getProperty("shiny.proxy.heartbeat-rate", "10000"));
		map.put("heartbeatPath", contextPath + "/heartbeat");
		
		return "app";
	}
	
	@RequestMapping(value="/app/*", method=RequestMethod.POST)
	@ResponseBody
	String startApp(HttpServletRequest request) {
		String mapping = dockerService.getMapping(request, getUserName(request), getAppName(request), true);
		return buildContainerPath(mapping, request);
	}
	
	private String buildContainerPath(String mapping, HttpServletRequest request) {
		if (mapping == null) return "";
		
		String queryString = request.getQueryString();
		queryString = (queryString == null) ? "" : "?" + queryString;
		
		String contextPath = ShinyProxyApplication.getContextPath(environment);
		String containerPath = contextPath + "/" + mapping + environment.getProperty("shiny.proxy.landing-page") + queryString;
		return containerPath;
	}

	@RequestMapping(value="/hcput", method=RequestMethod.GET)
	private String hazelcastPut() {

		dockerService.addElementToList();
    System.out.println("in put");
		return "app";
	}

	@RequestMapping(value="/hcget", method=RequestMethod.GET)
	private String hazelcastget() {

    System.out.println("in get");
    dockerService.getElement();
		return "app";
	}


}
