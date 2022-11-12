package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.Car;
import softuni.exam.models.dto.CarSeedDto;
import softuni.exam.repository.CarRepository;
import softuni.exam.service.CarService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class CarServiceImpl implements CarService {

    private final Gson gson;
    private final ModelMapper modelMapper;
    private final CarRepository carRepository;
    private final ValidationUtil validationUtil;

    private static final String PATH_OF_CARS = "src/main/resources/files/json/cars.json";

    public CarServiceImpl(Gson gson, ModelMapper modelMapper, CarRepository carRepository, ValidationUtil validationUtil) {
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.carRepository = carRepository;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return carRepository.count() > 0;
    }

    @Override
    public String readCarsFileContent() throws IOException {
        return Files.readString(Path.of(PATH_OF_CARS));
    }

    @Override
    public String importCars() throws IOException {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(gson.fromJson(readCarsFileContent(), CarSeedDto[].class))
                .filter(carSeedDto -> {
                    boolean isValid = validationUtil.isValid(carSeedDto);

                    sb.append(isValid ? String.format("Successfully imported car - %s - %s", carSeedDto.getMake(), carSeedDto.getModel())
                            : "Invalid car").append(System.lineSeparator());

                    return isValid;
                }).map(carSeedDto -> modelMapper.map(carSeedDto, Car.class))
                .forEach(carRepository::save);

        return sb.toString();
    }

    @Override
    public String getCarsOrderByPicturesCountThenByMake() {
        StringBuilder sb=new StringBuilder();

        carRepository.findAllCarsByPicturesCountThenByMake()
                .stream().forEach(car -> {
                    sb.append(String.format("Car make - %s, model - %s\n" +
                            "\tKilometers - %d\n" +
                            "\tRegistered on - %s\n" +
                            "\tNumber of pictures - %d\n",car.getMake(),
                            car.getModel(),
                            car.getKilometers(),
                            car.getRegisteredOn(),
                            car.getPictures().size())).append(System.lineSeparator());
                });
        return sb.toString();
    }

    @Override
    public Car findById(Long car) {
        return carRepository.findById(car).orElse(null);
    }
}
