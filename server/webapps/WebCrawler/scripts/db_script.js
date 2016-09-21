function loadPage() {
	$(window).bind("resize", (function() {
		if (this == event.target) {
			alignHeight();
			alignMargins();
		}
	}));
	
	$("#view-schema")[0].onclick = function() {
		$("#db-schema-modal-container").css("display", "block");
		$("#db-schema-close-button").css("display", "block");
		$("#db-schema-content").css("display", "block");		
		$("#modal-background").css("display", "block");
	}

	$(".modal-close-button").click(function() {
		hideModalContent();
	});

	$(document).keydown(function(e) { 
		if (e.keyCode == 27) { 
			if (!contentLoading) {
				hideModalContent();
			}
		} 
	});
	
	$("#query-input").resizable({
		handles: "s",
		minHeight: 100,
		maxHeight: 300,
		resize: function(event, ui ) {
			alignHeight(); 			
		}
	});
	
	$("#query-submit-button")[0].addEventListener("click", function() {
		$("#query-results-container").css("visibility", "hidden");
		$("#query-values-scrollbox").scrollTop(0);
		
		var queryInput = $("#query-input")[0];
		var selectedQuery = (queryInput.value.substring(queryInput.selectionStart, queryInput.selectionEnd));
		
		var stQuery;
		if (selectedQuery.length > 0) 
		{
			stQuery = selectedQuery;
		}
		else {
			stQuery = $("#query-input").val();
		}

		executeUserQuery(stQuery);
	});
	
	$("#query-values-scrollbox").scroll(function() {
		$("#query-column-headers-container").prop("scrollLeft", this.scrollLeft);
	});
	
	findLatestCrawl();
	loadDBSchema();
	loadSavedQueries();
	loadReports();

	alignMargins();
	alignHeight();
}

function findLatestCrawl() {
	$.ajax({
		type: "POST",
		url: "WebCrawler",
		dataType: "json",
		data: {
			request: "appDBQuery",
			appQueryName: "SelectLatestCompleteWebCrawlRun",
		},

		success: function( data, textStatus, jqXHR ) {	
			$.each(data, function () {
				$.each(this, function (name, value) {
					var stUnit = "hour";
					
					if (value != 1) {
						stUnit += "s";
					}
					
					$("#latest-crawl").text("Last crawled " + value + " " + stUnit + " ago.");
				});	
			});
		},
		 
		error: function(jqXHR, textStatus, errorThrown){
			 console.log(errorThrown);
		},
	});
}

function loadDBSchema() {
	$.get("./content/db_schema.txt", function(data) {
		var aStLines = data.split("\n");
		var stDBSchema = "";

		$.each(aStLines, function(n, stLine) {
			stDBSchema += (stLine + "<br>");
		});
		
		$("#db-schema-content").html(stDBSchema);
	});
}

var savedQueries = [];
function loadSavedQueries() {
	$.ajax({
		type: "GET",
		url: "./content/saved_queries.json",
		dataType: "json",
		success: function(data) {
			var nQuery = 0;
			
			$.each(data, function () {	
				var stCategory = this.saved_queries_category;				
				var secondLevelColumn = $('<a class="second-level-dropdown-column">' + stCategory + "</a>");
				var secondLevelContent = $('<ul class="second-level-dropdown-content"></ul>');
				
				$.each(this.saved_queries, function() {
					var stQueryName = this.query_name;
					var stQueryValue = this.query_value;

					savedQueries.push(stQueryValue);
					secondLevelContent.append("<a onclick=showSavedQuery(" + nQuery + ') class="saved-query">' + stQueryName + "</a>");
					
					nQuery++;
				});	
				
				secondLevelColumn.append(secondLevelContent);
				$("#saved-queries-content").append(secondLevelColumn);
			});
		},
		
		error: function(jqXHR, textStatus, errorThrown){
			console.log(errorThrown);
		}
	});
}

var reports = [];
function loadReports() {
		$.ajax({
		type: "GET",
		url: "./content/reports.json",
		dataType: "json",
		success: function(data) {
			var nReport = 0;
			$.each(data, function () {
				var stReportName = this.report_name;
				
				reports.push(this);
				$("#reports-content").append("<a onclick=getReport(" + nReport + ")>" + stReportName + "</a>");
				nReport++;
			});
		},
		
		error: function(jqXHR, textStatus, errorThrown){
			console.log(errorThrown);
		}
	});
}

function alignMargins() {
	var headerWidth = $("#aligned-content").width();	
	$("#query-input-container").css("width", headerWidth);
	$("#query-input").css("width", headerWidth - 4);
	$("#query-column-headers-container").css("width", headerWidth);
	$(".ui-wrapper").css("width", headerWidth);
}

function alignHeight() {
	var windowHeight = $(window).height();
	var queryColumnHeadersPosition = $("#query-column-headers-container").position().top;
	var newQueryValuesHeight = windowHeight - queryColumnHeadersPosition - 70;
	$("#query-values-container").css("max-height", newQueryValuesHeight);
	$(".ui-wrapper").css("margin-bottom", -10);
}

function showSavedQuery(nQuery) {
	var stQuery = savedQueries[nQuery];
	$("#query-input").val(stQuery);
	$("#query-input")[0].scrollTop = 0;
}

function getReport(nReport) {
	showLoadingMessage();
	
	$.ajax({
		type: "POST",
		url: "WebCrawler",
		dataType: "text",
		data: {
			request: "spreadsheetReport",
			reportFormat: JSON.stringify(reports[nReport])
		},

		success: function(data, textStatus, jqXHR) {			
			var reportPath = data;
			var reportFileName = reportPath.replace(/^.*[\\\/]/, '');
			var downloadLink = '<a id="file-download-link" href="' + reportPath + '" class="modal-temp" onclick="hideModalContent()">' + reportFileName + "</a>";			
			
			hideModalContent();
			showPopUpMessage("Click to download: ");
			
			$("#pop-up-message-content").css("display", "inline-block");
			$("#pop-up-message-modal-container").append(downloadLink);				
		},
		 
		error: function(jqXHR, textStatus, errorThrown){
			hideModalContent();
			showPopUpMessage(errorThrown);
		},
		 
		complete: function(jqXHR, textStatus){
			findLatestCrawl();
		}
	});
}

function hideModalContent() {	
	$(".modal-container").each(function(i, obj) {
		obj.scrollTop = 0;
	});
	
	$("#modal-background").css("display", "none");
	$(".modal-container").css("display", "none");
	$(".modal-content").css("display", "none");
	$(".modal-temp").remove();
	
	contentLoading = false;
}

function executeUserQuery(stQuery) {
	showLoadingMessage();
	
	$.ajax({
		type: "POST",
		url: "WebCrawler",
		dataType: "json",
		data: {
			request: "userDBQuery",
			userQuery: stQuery,
		},

		success: function(data, textStatus, jqXHR) {
			$("tr").remove();
			
			var queryValues = $("#query-values");
			var headersAdded = false;
			var queryColumnHeaders = $("#query-column-headers");
			var trHeader = "<tr>";
			
			$.each(data, function () {
				var trRow = '<tr class="query-value-row">';
				
				$.each(this, function (name, value) {
					if (!headersAdded) {
						var th = '<th id="' + name + '">' + name + "</th>";
						trHeader += th;
					}	
					
					var td = '<td class="' + name + '">' + value + "</td>";
					trRow += td;
				});
				
				if (!headersAdded) {
					trHeader += '<th id="query-column-headers-spacing">__</th></tr>';
					queryColumnHeaders.append(trHeader);
					headersAdded = true;
				}
				
				trRow += "</tr>";
				queryValues.append(trRow);
				
				$("#query-results-container").css("visibility", "visible");
			});
			
			hideModalContent();
			
			if (!headersAdded) {
				showPopUpMessage("This query returned no rows.");
			}
		},
		 
		error: function(jqXHR, textStatus, errorThrown){
			hideModalContent();
			showPopUpMessage(errorThrown);
		},
		 
		complete: function(jqXHR, textStatus){
			alignColumns();
			alignHeight();
			findLatestCrawl();
		}
	});
}

function alignColumns() {
	$("th").each(function () {
		var targetColumn = '[class="' + this.id + '"]';
		var startingColumnWidth = $(this).width();
		
		$(targetColumn).css("min-width", startingColumnWidth);
		$(targetColumn).css("width", startingColumnWidth);
		$(targetColumn).css("max-width", startingColumnWidth);
		
		var initialContainerWidth = $("#query-column-headers-container").width();
			
		$(this).resizable({
			handles: "e",
			minWidth: startingColumnWidth,
			resize: function( event, ui ) {		
				var containerExpansion = ui.size.width - initialContainerWidth;
				$("#query-values-container").width(containerExpansion);

				var newTargetColumnWidth = ui.size.width;
				$(this).css("min-width", newTargetColumnWidth);	
				$(targetColumn).css("width", newTargetColumnWidth);
				$(targetColumn).css("max-width", newTargetColumnWidth);
				$(targetColumn).css("min-width", newTargetColumnWidth);
			},
		})
	});
};	

var contentLoading = false;
function showLoadingMessage() {
	contentLoading = true;
	showPopUpMessage("Loading...");
	$("#pop-up-message-close-button").css("display", "none");
}

function showPopUpMessage(stMessage) {
	$("#pop-up-message-content").text(stMessage);
	$("#pop-up-message-modal-container").css("display", "block");
	$("#pop-up-message-content").css("display", "block");
	$("#pop-up-message-close-button").css("display", "block");
	$("#modal-background").css("display", "block");
	$("#pop-up-message-modal-container").scrollTop(0);
}
