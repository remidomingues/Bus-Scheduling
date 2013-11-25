package dataLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import locationsData.Bus;
import locationsData.Location;
import locationsData.Path;
import locationsData.Person;
import locationsData.TrafficInformation;
import antColonyOptimization.Partition;
import antColonyOptimization.Route;
import application.BusScheduling;
import application.BusSchedulingException;

/** 
 * XLS parser used for parsing people and interest points locations saved in XLS files.
 */
public class XLSParser
{
	/** Constant which define what is the string met when a people has a wheel chair */
	private static final String WHEEL_CHAIRED = "sim";

	/** Constant which define what is the string met when a people has a wheel chair */
	private static final String NON_WHEEL_CHAIRED = "não";

	/** Number of rows containing traffic informations */
	private static final int TRAFFIC_ROWS = 24;

	/** Number of columns containing traffic informations */
	private static final int TRAFFIC_COLS = 6;

	/** Each traffic time frame has a duration value defined by this attribute */
	private static final int TRAFFIC_STEP = 10 * 60;

	/**
	 * Parse the XLS configuration file
	 * @param file XLS file path
	 * @param times A schedule time which will be filled
	 * @param bus A bus list to fill
	 * @param traffic the traffic information to fill
	 * @throws BiffException if an error occurred when getting informations from the XLS file
	 * @throws IOException if an input / output exception occurs
	 * @throws BusSchedulingException 
	 */
	public void parseConfiguration(File file, Integer[] times, List<Bus> bus, TrafficInformation traffic)
			throws BiffException, IOException, BusSchedulingException
	{
		int row = 20, nbWheelChairs, nbSeats, minBusStartHour;
		Float lat1, lon1, lat2, lon2;
		String tmpNbWheelChairs, tmpNbSeats, id, drivers, fone, name1, street1, district1, tmpLat1, name2, street2, district2, tmpLon1, tmpLat2, tmpLon2, tmpMinBusStartHour, empty = "";
		Location loc1, loc2;
		Workbook w = getXLSScheet(file);
		Sheet sheet = w.getSheet(0);

		// 3 first arrays
		try
		{
			times[0] = new Integer(Integer.parseInt(sheet.getCell(1, 5).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException(
					"O tempo necessário para tomar/depositar uma pessoa"
							+ " sem cadeirante e errado (linha 6). (The time requested to take a person with a wheel chair is wrong)");
		}

		try
		{
			times[1] = new Integer(Integer.parseInt(sheet.getCell(1, 6).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException(
					"O tempo necessário para tomar/depositar uma pessoa"
							+ " com cadeirante e errado (linha 7). (The time requested to take a person without wheel chair is wrong)");
		}

		try
		{
			times[2] = new Integer(Integer.parseInt(sheet.getCell(1, 9).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException("O tempo necessário para fazer o troca de motorista"
					+ " e errado (linha 10). (The time requested to take swap drivers is wrong)");
		}

		try
		{
			times[3] = new Integer(Integer.parseInt(sheet.getCell(1, 10).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException("O tempo de cafe e errado (linha 11). (Coffe time is wrong)");
		}

		try
		{
			times[4] = new Integer(Integer.parseInt(sheet.getCell(1, 11).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException("O tempo de janta e errado (linha 12). (Dinner time is wrong)");
		}

		try
		{
			times[5] = new Integer(Integer.parseInt(sheet.getCell(0, 14).getContents()) * 3600
					+ Integer.parseInt(sheet.getCell(0, 15).getContents()) * 60);
		}
		catch (NumberFormatException e)
		{
			throw new BusSchedulingException("O número de horas de trabalho por dia por motorista e errado"
					+ " (linhas 15-16). (Driver work time is wrong)");
		}

		// Bus configuration
		lat1 = null;
		lon1 = null;
		lat2 = null;
		lon2 = null;
		id = sheet.getCell(0, row).getContents();
		drivers = sheet.getCell(1, row).getContents();
		fone = sheet.getCell(2, row).getContents();

		tmpNbWheelChairs = sheet.getCell(3, row).getContents();
		tmpNbSeats = sheet.getCell(4, row).getContents();
		tmpMinBusStartHour = sheet.getCell(16, row).getContents();
		name1 = sheet.getCell(6, row).getContents();
		street1 = sheet.getCell(7, row).getContents();
		district1 = sheet.getCell(8, row).getContents();
		tmpLat1 = sheet.getCell(9, row).getContents();
		tmpLon1 = sheet.getCell(10, row).getContents();

		name2 = sheet.getCell(11, row).getContents();
		street2 = sheet.getCell(12, row).getContents();
		district2 = sheet.getCell(13, row).getContents();
		tmpLat2 = sheet.getCell(14, row).getContents();
		tmpLon2 = sheet.getCell(15, row).getContents();

		while (row < sheet.getRows() && !id.equals(empty) && !drivers.equals(empty) && !fone.equals(empty)
				&& !name1.equals(empty) && !street1.equals(empty) && !district1.equals(empty)
				&& !name2.equals(empty) && !street2.equals(empty) && !district2.equals(empty)
				&& !tmpNbWheelChairs.equals(empty) && !tmpNbSeats.equals(empty)
				&& !tmpMinBusStartHour.equals("0"))
		{
			try
			{
				nbWheelChairs = Integer.parseInt(tmpNbWheelChairs);
			}
			catch (NumberFormatException e)
			{
				throw new BusSchedulingException("O número de cadeirantes e errado (linha " + (row + 1)
						+ "). (The wheel chairs number is wrong)");
			}

			try
			{
				nbSeats = Integer.parseInt(tmpNbSeats);
			}
			catch (NumberFormatException e)
			{
				throw new BusSchedulingException("O número de sedes e errado (linha " + (row + 1)
						+ "). (The seats number is wrong)");
			}

			if (!tmpMinBusStartHour.equals("0"))
			{
				try
				{
					minBusStartHour = Integer.parseInt(tmpMinBusStartHour);
				}
				catch (NumberFormatException e)
				{
					throw new BusSchedulingException("O hora disponível da partida e errado (linha "
							+ (row + 1) + "). (The minimal available bus hour is wrong)");
				}
			}
			else
			{
				minBusStartHour = Path.UNSPECIFIED_VALUE;
			}

			try
			{
				if (!tmpLat1.equals(empty))
				{
					lat1 = Float.parseFloat(tmpLat1);
				}
				if (!tmpLon1.equals(empty))
				{
					lon1 = Float.parseFloat(tmpLon1);
				}
				if (!tmpLat2.equals(empty))
				{
					lat2 = Float.parseFloat(tmpLat2);
				}
				if (!tmpLon2.equals(empty))
				{
					lon2 = Float.parseFloat(tmpLon2);
				}
			}
			catch (NumberFormatException e)
			{
				throw new BusSchedulingException("As côordenadas são erradas. (linha " + (row + 1)
						+ "). (Coordinates are wrong)");
			}

			if (lat1 != null && lon1 != null)
			{
				loc1 = new Location(name1, street1, district1, null, null, lat1, lon1);
			}
			else
			{
				loc1 = new Location(name1, street1, district1, null, null);
			}

			if (lat2 != null && lon2 != null)
			{
				loc2 = new Location(name2, street2, district2, null, null, lat2,
						lon2);
			}
			else
			{
				loc2 = new Location(name2, street2, district2, null, null);
			}

			bus.add(new Bus(id, drivers, fone, nbWheelChairs, nbSeats, loc1, loc2, minBusStartHour));

			++row;
			if (row < sheet.getRows())
			{
				lat1 = null;
				lon1 = null;
				lat2 = null;
				lon2 = null;
				id = sheet.getCell(0, row).getContents();
				drivers = sheet.getCell(1, row).getContents();
				fone = sheet.getCell(2, row).getContents();

				tmpNbWheelChairs = sheet.getCell(3, row).getContents();
				tmpNbSeats = sheet.getCell(4, row).getContents();
				tmpMinBusStartHour = sheet.getCell(16, row).getContents();
				name1 = sheet.getCell(6, row).getContents();
				street1 = sheet.getCell(7, row).getContents();
				district1 = sheet.getCell(8, row).getContents();
				tmpLat1 = sheet.getCell(9, row).getContents();
				tmpLon1 = sheet.getCell(10, row).getContents();

				name2 = sheet.getCell(11, row).getContents();
				street2 = sheet.getCell(12, row).getContents();
				district2 = sheet.getCell(13, row).getContents();
				tmpLat2 = sheet.getCell(14, row).getContents();
				tmpLon2 = sheet.getCell(15, row).getContents();
			}
		}
		if (row < sheet.getRows()
				&& (!id.equals(empty) || !drivers.equals(empty) || !fone.equals(empty)
						|| !name1.equals(empty) || !street1.equals(empty) || !district1.equals(empty)
						|| !name2.equals(empty) || !street2.equals(empty) || !district2.equals(empty)
						|| !tmpNbWheelChairs.equals(empty) || !tmpNbSeats.equals(empty)
						|| !tmpLat1.equals(empty) || !tmpLon1.equals(empty) || !tmpLat2.equals(empty)
						|| !tmpLon2.equals(empty) || !tmpMinBusStartHour.equals("0")))
			throw new BusSchedulingException("Um campo obrigatório falta (linha " + (row + 1)
					+ "). (A compulsory field is missing)");
		parseTrafficCoefficients(w.getSheet(1), traffic);
		w.close();
	}

	/**
	 * Parse the traffic coefficient from a sheet
	 * @param sheet The sheet to parse
	 * @param traffic the traffic to fill
	 * @throws BusSchedulingException If an exception must be threw to the user
	 */
	private void parseTrafficCoefficients(Sheet sheet, TrafficInformation traffic)
			throws BusSchedulingException
	{
		float coeff;
		String tmpCoeff;
		int trafficRow = 2, trafficStartHour = 0;
		traffic = new TrafficInformation(TRAFFIC_COLS * TRAFFIC_ROWS);

		for (int i = trafficRow ; i < trafficRow + TRAFFIC_ROWS ; ++i)
		{
			for (int j = 1 ; j < TRAFFIC_COLS + 1 ; ++j)
			{
				try
				{
					tmpCoeff = sheet.getCell(j, i).getContents();
					coeff = Float.parseFloat(tmpCoeff.replace(',', '.'));
				}
				catch (NumberFormatException e)
				{
					throw new BusSchedulingException("O coeficiente de tráfego e errado (linha " + (i + 1)
							+ "). (The traffic coefficient is wrong)");
				}

				try
				{
					traffic.addCoefficient(trafficStartHour, trafficStartHour + TRAFFIC_STEP, coeff);
				}
				catch (IllegalArgumentException e)
				{
					throw new BusSchedulingException(e.getMessage() + (" (linha " + (i + 1) + ")"));
				}
				trafficStartHour += TRAFFIC_STEP;
			}
		}
	}

	/**
	 * Parse paths from a XLS file
	 * @param file The path of the XLS file
	 * @param sheetNumber Sheet number
	 * @return An array of paths read
	 * @throws BiffException if an error occurred when getting informations from the XLS file
	 * @throws IOException if an input / output exception occurs
	 * @throws BusSchedulingException 
	 */
	public List<Path> parsePaths(File file, int sheetNumber) throws BiffException, IOException,
			BusSchedulingException
	{
		int row = 9, minTakeHour, dropOffHour;
		Float originLat, originLon, destLat, destLon;
		List<Path> paths = new ArrayList<Path>();
		Workbook w = getXLSScheet(file);
		Sheet sheet = w.getSheet(sheetNumber);
		String empty = "", tmpWheelChair, personName, tmpMinTakeHour, tmpDropOffHour, originLocationName, originLocationStreet, originLocationDistrict, originLocationInformation, tmpOriginLat, tmpOriginLon, destLocationName, destLocationStreet, destLocationDistrict, destLocationInformation, tmpDestLat, tmpDestLon;
		boolean wheelChair;
		Person person;
		Location origin, destination;

		originLat = null;
		originLon = null;
		destLat = null;
		destLon = null;

		personName = sheet.getCell(0, row).getContents();
		tmpWheelChair = sheet.getCell(1, row).getContents();
		tmpMinTakeHour = sheet.getCell(16, row).getContents();
		tmpDropOffHour = sheet.getCell(17, row).getContents();

		originLocationName = sheet.getCell(4, row).getContents();
		originLocationStreet = sheet.getCell(5, row).getContents();
		originLocationDistrict = sheet.getCell(6, row).getContents();
		originLocationInformation = sheet.getCell(7, row).getContents();
		tmpOriginLat = sheet.getCell(8, row).getContents();
		tmpOriginLon = sheet.getCell(9, row).getContents();

		destLocationName = sheet.getCell(10, row).getContents();
		destLocationStreet = sheet.getCell(11, row).getContents();
		destLocationDistrict = sheet.getCell(12, row).getContents();
		destLocationInformation = sheet.getCell(13, row).getContents();
		tmpDestLat = sheet.getCell(14, row).getContents();
		tmpDestLon = sheet.getCell(15, row).getContents();

		while (row < sheet.getRows() && !personName.equals(empty) && !tmpWheelChair.equals(empty)
				&& !tmpDropOffHour.equals(empty) && !originLocationStreet.equals(empty)
				&& !originLocationDistrict.equals(empty) && !destLocationStreet.equals(empty)
				&& !destLocationDistrict.equals(empty))
		{
			if (tmpWheelChair.equals(WHEEL_CHAIRED))
			{
				wheelChair = true;
			}
			else if (tmpWheelChair.equals(NON_WHEEL_CHAIRED))
			{
				wheelChair = false;
			}
			else
				throw new BusSchedulingException("O valor do campo cadeirante deve ser '" + WHEEL_CHAIRED
						+ "' o '" + NON_WHEEL_CHAIRED + "' (linha " + (row + 1)
						+ "). (The value of the wheel chaired field is false)");

			if (!tmpMinTakeHour.equals("0"))
			{
				try
				{
					minTakeHour = Integer.parseInt(tmpMinTakeHour);
				}
				catch (NumberFormatException e)
				{
					throw new BusSchedulingException("O hora mínimo da tomada é errado (linha " + (row + 1)
							+ "). (Minimal take time is malformed)");
				}
			}
			else
			{
				minTakeHour = Path.UNSPECIFIED_VALUE;
			}

			try
			{
				dropOffHour = Integer.parseInt(tmpDropOffHour);
			}
			catch (NumberFormatException e)
			{
				throw new BusSchedulingException("O hora de chegada é errado (linha " + (row + 1)
						+ "). (Drop off time is malformed)");
			}

			if (dropOffHour == 0)
			{
				throw new BusSchedulingException("O hora de chegada é obligatorio (linha " + (row + 1)
						+ "). (Drop off time is compulsory)");
			}

			person = new Person(personName, wheelChair);

			try
			{
				if (!tmpOriginLat.isEmpty() && !tmpOriginLon.isEmpty())
				{
					originLat = Float.parseFloat(tmpOriginLat);
					originLon = Float.parseFloat(tmpOriginLon);
					origin = new Location(originLocationName, originLocationStreet, originLocationDistrict,
							originLocationInformation, null, originLat, originLon);
				}
				else
				{
					origin = new Location(originLocationName, originLocationStreet, originLocationDistrict,
							originLocationInformation, null);
				}

				if (!tmpDestLat.isEmpty() && !tmpDestLon.isEmpty())
				{
					destLat = Float.parseFloat(tmpDestLat);
					destLon = Float.parseFloat(tmpDestLon);
					destination = new Location(destLocationName, destLocationStreet, destLocationDistrict,
							destLocationInformation, null, destLat, destLon);
				}
				else
				{
					destination = new Location(destLocationName, destLocationStreet, destLocationDistrict,
							destLocationInformation, null);
				}

				if (!tmpOriginLat.isEmpty() && !tmpOriginLon.isEmpty() && !tmpDestLat.isEmpty()
						&& !tmpDestLon.isEmpty() && origin.getLatitude() == destination.getLatitude()
						&& origin.getLongitude() == destination.getLongitude())
					throw new BusSchedulingException(
							"Um trajeto entrado possui côordenadas de partida"
									+ " e de destino idênticas (linha "
									+ (row + 1)
									+ "). Isto talvez causado por lugares não encontrados.\n(Same coordinates for departure and destination in a path entered. Please check locations entered)");
			}
			catch (NumberFormatException e)
			{
				throw new BusSchedulingException("As côordenadas são erradas (linha " + (row + 1)
						+ "). (Coordinates are wrong)");
			}

			try
			{
				paths.add(new Path(origin, destination, minTakeHour, dropOffHour, person));
			}
			catch (Exception e)
			{
				throw new BusSchedulingException(e.getMessage());
			}

			++row;
			if (row < sheet.getRows())
			{
				originLat = null;
				originLon = null;
				destLat = null;
				destLon = null;

				personName = sheet.getCell(0, row).getContents();
				tmpWheelChair = sheet.getCell(1, row).getContents();
				tmpMinTakeHour = sheet.getCell(16, row).getContents();
				tmpDropOffHour = sheet.getCell(17, row).getContents();

				originLocationName = sheet.getCell(4, row).getContents();
				originLocationStreet = sheet.getCell(5, row).getContents();
				originLocationDistrict = sheet.getCell(6, row).getContents();
				originLocationInformation = sheet.getCell(7, row).getContents();
				tmpOriginLat = sheet.getCell(8, row).getContents();
				tmpOriginLon = sheet.getCell(9, row).getContents();

				destLocationName = sheet.getCell(10, row).getContents();
				destLocationStreet = sheet.getCell(11, row).getContents();
				destLocationDistrict = sheet.getCell(12, row).getContents();
				destLocationInformation = sheet.getCell(13, row).getContents();
				tmpDestLat = sheet.getCell(14, row).getContents();
				tmpDestLon = sheet.getCell(15, row).getContents();
			}
		}
		if (row < sheet.getRows()
				&& (!personName.equals(empty) || !tmpWheelChair.equals(empty) || !tmpMinTakeHour.equals("0")
						|| !tmpDropOffHour.equals("0") || !originLocationName.equals(empty)
						|| !originLocationStreet.equals(empty) || !originLocationDistrict.equals(empty)
						|| !originLocationInformation.equals(empty) || !tmpOriginLat.equals(empty)
						|| !tmpOriginLon.equals(empty) || !destLocationName.equals(empty)
						|| !destLocationStreet.equals(empty) || !destLocationDistrict.equals(empty)
						|| !destLocationInformation.equals(empty) || !tmpDestLat.equals(empty) || !tmpDestLon
							.equals(empty)))
			throw new BusSchedulingException("Um campo obrigatório falta (linha " + (row + 1)
					+ "). (A compulsory field is missing)");
		w.close();
		return paths;
	}

	/**
	 * Write bus locations coordinates in an XLS file
	 * @param file Output file
	 * @param bus Data
	 * @throws Exception 
	 */
	public synchronized void writeConfigurationCoordinates(File file, Bus bus[]) throws Exception
	{
		WritableCellFormat format;
		WorkbookSettings ws = new WorkbookSettings();
		ws.setSuppressWarnings(true);
		ws.setEncoding("ISO-8859-1");
		Workbook modelWorkbook = Workbook.getWorkbook(file, ws);
		WritableWorkbook workbook = Workbook.createWorkbook(file, modelWorkbook, ws);
		try
		{
			WritableSheet sheet = workbook.getSheet(0);
			int row = 20;

			for (Bus b : bus)
			{
				if (!b.getOrigin().isCoordinatesFilled())
				{
					format = new WritableCellFormat(sheet.getCell(9, row).getCellFormat());
					sheet.addCell(new Label(9, row, Float.toString(b.getOrigin().getLatitude()), format));

					format = new WritableCellFormat(sheet.getCell(10, row).getCellFormat());
					sheet.addCell(new Label(10, row, Float.toString(b.getOrigin().getLongitude()), format));
				}
				if (!b.getDriverSwap().isCoordinatesFilled())
				{
					format = new WritableCellFormat(sheet.getCell(14, row).getCellFormat());
					sheet.addCell(new Label(14, row, Float.toString(b.getDriverSwap().getLatitude()), format));

					format = new WritableCellFormat(sheet.getCell(15, row).getCellFormat());
					sheet.addCell(new Label(15, row, Float.toString(b.getDriverSwap().getLongitude()), format));
				}
				++row;
			}
			workbook.write();
			workbook.close();
			modelWorkbook.close();
		}
		catch (Exception e)
		{
			workbook.write();
			workbook.close();
			modelWorkbook.close();
			throw new Exception(e.getCause());
		}
	}

	/**
	 * Write locations coordinates in an XLS file
	 * @param file Output file
	 * @param paths Locations data
	 * @throws BusSchedulingException 
	 * @throws Exception 
	 */
	public synchronized void writePathsCoordinates(File file, List<Path> paths)
			throws BusSchedulingException, Exception
	{
		WritableCellFormat format;
		WorkbookSettings ws = new WorkbookSettings();
		ws.setSuppressWarnings(true);
		ws.setEncoding("ISO-8859-1");
		Workbook modelWorkbook = Workbook.getWorkbook(file, ws);
		WritableWorkbook workbook = Workbook.createWorkbook(file, modelWorkbook, ws);
		try
		{
			WritableSheet sheet = workbook.getSheet(0);
			int row = 9;

			for (Path p : paths)
			{
				if (p.getOrigin().getLatitude() == p.getDestination().getLatitude()
						&& p.getOrigin().getLongitude() == p.getDestination().getLongitude())
					throw new BusSchedulingException(
							"Um trajeto entrado possui côordenadas de partida"
									+ " e de destino idênticas (linha "
									+ (row + 1)
									+ "). Isto talvez causado por lugares não encontrados.\n(Same coordinates for departure and destination in a path entered. Please check locations entered)");

				if (!p.getOrigin().isCoordinatesFilled())
				{
					format = new WritableCellFormat(sheet.getCell(8, row).getCellFormat());
					sheet.addCell(new Label(8, row, Float.toString(p.getOrigin().getLatitude()), format));

					format = new WritableCellFormat(sheet.getCell(9, row).getCellFormat());
					sheet.addCell(new Label(9, row, Float.toString(p.getOrigin().getLongitude()), format));
				}

				if (!p.getDestination().isCoordinatesFilled())
				{
					format = new WritableCellFormat(sheet.getCell(14, row).getCellFormat());
					sheet.addCell(new Label(14, row, Float.toString(p.getDestination().getLatitude()), format));

					format = new WritableCellFormat(sheet.getCell(15, row).getCellFormat());
					sheet.addCell(new Label(15, row, Float.toString(p.getDestination().getLongitude()),
							format));
				}
				++row;
			}
			workbook.write();
			workbook.close();
			modelWorkbook.close();
		}
		catch (Exception e)
		{
			workbook.write();
			workbook.close();
			modelWorkbook.close();
			if (e instanceof BusSchedulingException)
				throw new BusSchedulingException(e.getMessage());
			else
				throw new Exception(e.getCause());
		}
	}

	/**
	 * Write local search results in an XLS file
	 * @param model XLS model file
	 * @param file Results output file
	 * @param bus Bus data
	 * @param partition Bus locations partition
	 * @throws Exception 
	 */
	public synchronized void writeResults(File model, File file, Bus bus[], Partition partition)
			throws Exception
	{
		WorkbookSettings ws = new WorkbookSettings();
		ws.setSuppressWarnings(true);
		ws.setEncoding("ISO-8859-1");
		Workbook modelWorkbook = Workbook.getWorkbook(model, ws);
		WritableWorkbook workbook = Workbook.createWorkbook(file, modelWorkbook, ws);
		try
		{
			if (bus.length == 0)
				return;

			for (int i = 1 ; i < bus.length ; ++i)
			{
				workbook.copySheet(0, "Bus " + (i + 1), i);
			}

			for (int i = 0 ; i < bus.length ; ++i)
			{
				writeRouteResults(workbook.getSheet(i), bus[i], partition.getRoutes().get(i));
			}

			workbook.write();
			workbook.close();
			modelWorkbook.close();
		}
		catch (Exception e)
		{
			workbook.write();
			workbook.close();
			modelWorkbook.close();

			throw new Exception(e.getCause());
		}
	}

	/**
	 * Write route's pieces of informations in a sheet
	 * @param sheet Output sheet
	 * @param bus Bus data
	 * @param route Route data
	 * @throws RowsExceededException If the number of rows is exceeded
	 * @throws WriteException If an error occurs when writing
	 */
	private synchronized void writeRouteResults(WritableSheet sheet, Bus bus, Route route)
			throws RowsExceededException, WriteException
	{
		WritableCellFormat format;
		int pathTime, delay, totalDelay, row = 12, modelRow = row + 1;
		float pathDistance;
		Location location;
		String destinationAddress;

		format = new WritableCellFormat(sheet.getCell(0, 4).getCellFormat());
		sheet.addCell(new Label(0, 4, "Número " + bus.getId() + " - " + bus.getNonWheelChairsSeats()
				+ " poltronas, " + bus.getWheelChairsSeats() + " cadeiras", format));
		format = new WritableCellFormat(sheet.getCell(0, 5).getCellFormat());
		sheet.addCell(new Label(0, 5, "Fone : " + bus.getPhone(), format));
		format = new WritableCellFormat(sheet.getCell(0, 6).getCellFormat());
		sheet.addCell(new Label(0, 6, "Motoristas : " + bus.getDrivers(), format));

		format = new WritableCellFormat(sheet.getCell(0, 7).getCellFormat());
		if (route == null)
		{
			sheet.addCell(new Label(0, 7, "Este ônibus não é utilizado.", format));
			return;
		}
		sheet.addCell(new Label(0, 7, "Hora de partida : "
				+ BusScheduling.longToHourString(route.getArrivalHour(0)), format));

		// Route informations
		pathTime = route.getTotalDuration();
		pathDistance = route.getTotalDistance() / 1000f;
		totalDelay = route.getTotalDelay();

		format = new WritableCellFormat(sheet.getCell(5, 4).getCellFormat());
		sheet.addCell(new Label(7, 4, String.format("%.3f km", pathDistance), format));
		format = new WritableCellFormat(sheet.getCell(5, 5).getCellFormat());
		sheet.addCell(new Label(7, 5, BusScheduling.longToDurationStringNoSeconds(pathTime), format));
		format = new WritableCellFormat(sheet.getCell(5, 6).getCellFormat());
		sheet.addCell(new Label(7, 6, String.format("%d", route.getCountTransportedPerson()), format));
		format = new WritableCellFormat(sheet.getCell(5, 7).getCellFormat());
		sheet.addCell(new Label(7, 7, String.format("%.2f passageiro(s) pelo quilômetro",
				route.getCountTransportedPerson() / pathDistance, format)));

		format = new WritableCellFormat(sheet.getCell(4, 8).getCellFormat());
		// Delay notification
		if (totalDelay > 0)
		{
			sheet.addCell(new Label(4, 8, "Atraso total : "
					+ BusScheduling.longToDurationStringNoSeconds(route.getTotalDelay()), format));
		}
		else
		{
			sheet.addCell(new Label(4, 8, "Não atraso.", format));
		}

		// Locations pieces of informations
		for (int i = 0 ; i < route.getCountStops() ; ++i)
		{
			if (i == route.getCountStops() - 1)
			{
				++modelRow;
			}
			sheet.insertRow(row);
			sheet.setRowView(row, 300);
			location = route.getLocation(i);
			delay = route.getDelay(i);

			format = new WritableCellFormat(sheet.getCell(0, modelRow).getCellFormat());
			sheet.addCell(new Label(0, row, BusScheduling.longToHourString(route.getArrivalHour(i)), format));

			format = new WritableCellFormat(sheet.getCell(1, modelRow).getCellFormat());
			if (delay != 0)
			{
				WritableFont font = new WritableFont(format.getFont());
				if (delay > 0)
					font.setColour(Colour.RED);
				else
					font.setColour(Colour.BLUE);
				format.setFont(font);
				sheet.addCell(new Label(1, row, BusScheduling.longToDurationStringNoSeconds(delay), format));
			}
			else
			{
				sheet.addCell(new Label(1, row, "-", format));
			}

			format = new WritableCellFormat(sheet.getCell(2, modelRow).getCellFormat());
			destinationAddress = "";
			if (route.isPickupStop(i))
			{
				sheet.addCell(new Label(2, row, "Tomada", format));
				try
				{
					destinationAddress = route.getDestination(i).getAddress();
				}
				catch (NullPointerException e)
				{}
			}
			else if (route.isDepositStop(i))
			{
				sheet.addCell(new Label(2, row, "Depósito", format));
			}
			else if (route.isDriverSwapStop(i))
			{
				sheet.addCell(new Label(2, row, "- Troca -", format));
			}
			else
			{
				sheet.addCell(new Label(2, row, "-", format));
			}
			
			format = new WritableCellFormat(sheet.getCell(8, modelRow).getCellFormat());
			sheet.addCell(new Label(8, row, destinationAddress, format));
			
			if (route.getPerson(i) == null)
			{
				format = new WritableCellFormat(sheet.getCell(3, modelRow).getCellFormat());
				sheet.addCell(new Label(3, row, "-", format));
				format = new WritableCellFormat(sheet.getCell(4, modelRow).getCellFormat());
				sheet.addCell(new Label(4, row, "-", format));
			}
			else
			{
				format = new WritableCellFormat(sheet.getCell(3, modelRow).getCellFormat());
				sheet.addCell(new Label(3, row, route.getPerson(i).getName(), format));
				format = new WritableCellFormat(sheet.getCell(4, modelRow).getCellFormat());
				if (route.getPerson(i).isWheelchaired())
				{
					sheet.addCell(new Label(4, row, "Sim", format));
				}
				else
				{
					sheet.addCell(new Label(4, row, "Não", format));
				}
			}

			format = new WritableCellFormat(sheet.getCell(5, modelRow).getCellFormat());
			sheet.mergeCells(5, row, 7, row);
			sheet.addCell(new Label(5, row, location.getAddress(), format));

			++row;
			++modelRow;
		}
		sheet.removeRow(row);
		sheet.removeRow(row);
	}

	/** Parse an XLS file given in parameter.
	 * @param file The XLS file path
	 * @param isPeople true if the file contains people locations, false else
	 * @return A locations array containing the stock classes filled
	 * @throws BiffException if an error occurred when getting informations from the XLS file
	 * @throws IOException if an input / output exception occurs
	 * @throws BusSchedulingException 
	 */
	public Object[] parseLocations(File file, boolean isPeople) throws BiffException, IOException,
			BusSchedulingException
	{
		Workbook w = getXLSScheet(file);
		Sheet sheet = w.getSheet(0);
		if (isPeople)
			return parsePeople(sheet);
		else
			return parseInterestPoints(sheet);
	}

	/**
	 * Return the XLS workbook in the specified XLS file
	 * @param xlsFile XLS file path
	 * @return An XLS workbook
	 * @throws BiffException if an error occured when getting informations from the XLS file
	 * @throws IOException if an input / output exception occurs
	 */
	private Workbook getXLSScheet(File xlsFile) throws BiffException, IOException
	{
		WorkbookSettings ws = new WorkbookSettings();
		ws.setSuppressWarnings(true);
		ws.setEncoding("ISO-8859-1");
		return Workbook.getWorkbook(xlsFile, ws);
	}

	// Unused
	/** 
	 * Parse people locations and fill stock classes from an XLS sheet.
	 * @param sheet XLS sheet containing people locations
	 * @return A locations array containing the stock classes filled
	 */
	private Person[] parsePeople(Sheet sheet)
	{
		Person[] people = new Person[sheet.getRows() - 2];
		for (int i = 2 ; i < sheet.getRows() ; ++i)
		{
			String name = sheet.getCell(0, i).getContents();
			// String street = sheet.getCell(2, i).getContents();
			// String district = sheet.getCell(3, i).getContents();
			boolean wheelchaired = sheet.getCell(1, i).getContents().equals(WHEEL_CHAIRED) ? true : false;
			people[i - 2] = new Person(name, wheelchaired);
		}
		return people;
	}

	// Unused
	/** 
	 * Parse Interest points locations and fill stock classes from an XLS sheet.
	 * @param sheet XLS sheet containing interest points locations
	 * @return A locations array containing the stock classes filled
	 * @throws BusSchedulingException 
	 */
	private Location[] parseInterestPoints(Sheet sheet) throws BusSchedulingException
	{
		Location[] locations = new Location[sheet.getRows() - 3];
		for (int i = 3 ; i < sheet.getRows() ; ++i)
		{
			String name = sheet.getCell(0, i).getContents();
			String street = sheet.getCell(1, i).getContents();
			String district = sheet.getCell(2, i).getContents();
			String phoneNumber = sheet.getCell(3, i).getContents();
			locations[i - 3] = new Location(name, street, district, null, phoneNumber);
		}
		return locations;
	}
}
