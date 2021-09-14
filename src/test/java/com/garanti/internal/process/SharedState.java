package com.garanti.internal.process;

import io.cucumber.guice.ScenarioScoped;

@ScenarioScoped
public class SharedState {
    
    private Long processId;
    
    private String processDefinitionId;

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
}