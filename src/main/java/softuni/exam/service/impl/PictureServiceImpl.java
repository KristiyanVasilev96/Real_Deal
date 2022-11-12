package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.Picture;
import softuni.exam.models.dto.PictureSeedDto;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.CarService;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PictureServiceImpl implements PictureService {
    private final PictureRepository pictureRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

   private final CarService carService;

    private static final String PATH_OF_PICTURES = "src/main/resources/files/json/pictures.json";

    public PictureServiceImpl(PictureRepository pictureRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil,  CarService carService) {
        this.pictureRepository = pictureRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.carService = carService;

    }

    @Override
    public boolean areImported() {
        return pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return Files.readString(Path.of(PATH_OF_PICTURES));
    }

    @Override
    public String importPictures() throws IOException {
        StringBuilder sb=new StringBuilder();

        Arrays.stream(gson.fromJson(readPicturesFromFile(), PictureSeedDto[].class))
                .filter(pictureSeedDto -> {
                    boolean isValid=validationUtil.isValid(pictureSeedDto);
                    sb.append(isValid ? String.format("Successfully import picture - %s",pictureSeedDto.getName())
                            : "Invalid picture").append(System.lineSeparator());


                    return isValid;
                }).map(pictureSeedDto -> {
                    Picture picture=modelMapper.map(pictureSeedDto,Picture.class);
                    picture.setCar(carService.findById(pictureSeedDto.getCar()));

                    return picture;
                }).forEach(pictureRepository::save);


        return sb.toString();
    }
}
