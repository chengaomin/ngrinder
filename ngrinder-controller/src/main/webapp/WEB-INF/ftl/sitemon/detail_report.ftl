<fieldSet>
	<legend><@spring.message "siteMon.report.testResult"/></legend>
</fieldSet>
<div id="test_result_chart" class="chart"></div>
<div class="row form-horizontal">
	<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="siteMon.report.error.log">
		<div id="error_log" class="div-logs"></div>
	</@control_group>
</div>
<fieldSet>
	<legend><@spring.message "siteMon.report.testTime"/></legend>
</fieldSet>
<div id="test_time_chart" class="chart"></div>