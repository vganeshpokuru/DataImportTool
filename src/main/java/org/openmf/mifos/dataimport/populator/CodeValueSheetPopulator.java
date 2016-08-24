package org.openmf.mifos.dataimport.populator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openmf.mifos.dataimport.dto.CompactCodeValue;
import org.openmf.mifos.dataimport.handler.Result;
import org.openmf.mifos.dataimport.http.RestClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class CodeValueSheetPopulator extends AbstractWorkbookPopulator {

	private String content;
	private final RestClient restClient;
	private List<CompactCodeValue> codeValues;
	private final String codeName;
	private static final int CODE_VALUE_NAME_COL = 0;
	private static final int CODE_VALUE_ID_COL = 1;

	public CodeValueSheetPopulator(RestClient restClient) {
		super();
		this.restClient = restClient;
		codeValues = new ArrayList<CompactCodeValue>();
		this.codeName = "GuarantorRelationship";
	}

	@Override
	public Result downloadAndParse() {
		Result result = new Result();
		try {
			restClient.createAuthToken();
			content = restClient.get("codes/codeValues?codeName=" + getCodeName());
			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			JsonArray array = parser.parse(content).getAsJsonArray();
			Iterator<JsonElement> iterator = array.iterator();
			while (iterator.hasNext()) {
				JsonElement json = iterator.next();
				CompactCodeValue codeValue = gson.fromJson(json,
						CompactCodeValue.class);
				codeValues.add(codeValue);
			}
		} catch (Exception e) {
			result.addError(e.getMessage());

		}
		return result;
	}

	@Override
	public Result populate(Workbook workbook) {
		Result result = new Result();
		try {
			int rowIndex = 1;
			Sheet officeSheet = workbook.createSheet("CodeValues");
			setLayout(officeSheet);

			populateCodeValues(officeSheet, rowIndex);
			officeSheet.protectSheet("");
		} catch (Exception e) {
			result.addError(e.getMessage());

			e.printStackTrace();
		}
		return result;
	}

	private void populateCodeValues(Sheet officeSheet, int rowIndex) {
		for (CompactCodeValue codeValue : codeValues) {
			Row row = officeSheet.createRow(rowIndex);
			writeInt(CODE_VALUE_ID_COL, row, codeValue.getId());
			writeString(CODE_VALUE_NAME_COL, row, codeValue.getName().trim()
					.replaceAll("[ )(]", "_"));
			rowIndex++;
		}
	}

	private void setLayout(Sheet worksheet) {
		worksheet.setColumnWidth(CODE_VALUE_ID_COL, 2000);
		worksheet.setColumnWidth(CODE_VALUE_NAME_COL, 7000);
		Row rowHeader = worksheet.createRow(0);
		rowHeader.setHeight((short) 500);
		writeString(CODE_VALUE_ID_COL, rowHeader, "ID");
		writeString(CODE_VALUE_NAME_COL, rowHeader, "Name");
	}

	public List<CompactCodeValue> getCodeValues() {
		return codeValues;
	}

	public String getCodeName() {
		return codeName;
	}

}
