<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Global Properties"
	otherwise="/login.htm" redirect="/module/testing/settings.form" />
<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>
	<spring:message code="testing.settings.title" />
</h2>
<table style="width: 98%;">
	<tr style="vertical-align: top;">
		<td class="box"><form method="post">
				<table>
					<c:forEach items="${ settingsForm.settings }" var="item"
						varStatus="status">
						<tr>
							<td style="width: 70%;">${ item.name } <br /> <span
								class="description">${ item.globalProperty.description }</span>
							</td>
							<td><input type="text" value="${ item.globalProperty.propertyValue }"
									name="settings[${status.index}].globalProperty.propertyValue"
									id="settings${status.index}.globalProperty.propertyValue"
									size="50" maxlength="4000" /></td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2"><p>
								<input id="saveButton" type="submit"
									value="<spring:message code="general.save"/>" /> <input
									id="cancelButton" type="button"
									value="<spring:message code="general.cancel"/>"
									onclick="window.location=''" />
							</p></td>
					</tr>
				</table>
			</form></td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>