import xlsxwriter


if _name_ == "_main_":
  with open("Variables.tsv", "r") as in_file:
    cells = in_file.readline()
    array_of_cells = cells.split(' ')
    workbook = xlsxwriter.Workbook('matrix.xlsx')
    worksheet = workbook.add_worksheet()
    excel_letter = "A"
    excel_number = 1
    for (idx, cell) in enumerate(array_of_cells):
      if (idx % 20 == 0 and idx != 0):
        excel_letter = chr(ord(excel_letter) + 1)
        excel_number = 1
      worksheet.write(f"{excel_letter}{excel_number}", cell)
      excel_number += 1
    workbook.close()