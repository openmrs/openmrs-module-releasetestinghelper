<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Global Properties"
	otherwise="/login.htm" redirect="/module/testing/settings.form" />
<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>
	<spring:message code="testing.settings.title" />
</h2>
<table style="width: 98%;">
	<tr style="vertical-align: top;">
		<td class="box"><form:form method="post"
				commandName="settingsForm">
				<table>
					<c:forEach items="${ settingsForm.settings }" var="item"
						varStatus="status">
						<tr>
							<td style="width: 70%;">${ item.name } <br /> <span
								class="description">${ item.globalProperty.description }</span>
							</td>
							<td>
								<c:choose>
									<c:when test="${ not empty item.globalProperty.datatypeClassname }">
										<input type="hidden" name="originalValue[${ status.index }]" value='<c:out escapeXml="true" value="${ item.globalProperty.propertyValue }" />'/>
										<openmrs_tag:singleCustomValue
											formFieldName="settings[${ status.index }].globalProperty.propertyValue"
											value="${ item.globalProperty }" />
									</c:when>
									<c:otherwise>
										<form:input
											path="settings[${status.index}].globalProperty.propertyValue"
											size="50" maxlength="4000" /></td>
									</c:otherwise>
								</c:choose>
								<form:errors path="settings[${status.index}].globalProperty.propertyValue" cssClass="error"/>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2"><p>
								<input id="saveButton" type="submit"
									value="<spring:message code="general.save"/>" />
								<input id="cancelButton" type="button"
									value="<spring:message code="general.cancel"/>"
									onclick="window.location=''" />
							</p></td>
					</tr>
				</table>
			</form:form></td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>