package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.Offer;
import softuni.exam.models.dto.OfferSeedRootDto;
import softuni.exam.repository.OfferRepository;
import softuni.exam.service.CarService;
import softuni.exam.service.OfferService;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OfferServiceImpl implements OfferService {
    private final OfferRepository offerRepository;
    private final CarService carService;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;

    private final SellerService sellerService;

    private static final String PATH_OF_OFFERS = "src/main/resources/files/xml/offers.xml";

    public OfferServiceImpl(OfferRepository offerRepository, CarService carService, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil, SellerService sellerService) {
        this.offerRepository = offerRepository;
        this.carService = carService;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;

        this.validationUtil = validationUtil;
        this.sellerService = sellerService;
    }

    @Override
    public boolean areImported() {
        return offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Path.of(PATH_OF_OFFERS));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder sb=new StringBuilder();

        xmlParser.fromFile(PATH_OF_OFFERS, OfferSeedRootDto.class)
                .getOffers()
                .stream().filter(offerSeedDto -> {
                    boolean isValid=validationUtil.isValid(offerSeedDto);

                    sb.append(isValid ? String.format("Successfully import offer %s - %s",
                            offerSeedDto.getAddedOn(),offerSeedDto.getHasGoldStatus())
                            : "Invalid offer").append(System.lineSeparator());

                    return isValid;
                }).map(offerSeedDto -> {
                    Offer offer=modelMapper.map(offerSeedDto,Offer.class);
                    offer.setSeller(sellerService.findById(offerSeedDto.getSeller().getId()));
                    offer.setCar(carService.findById(offerSeedDto.getCar().getId()));
                    return offer;
                }).forEach(offerRepository::save);

        return sb.toString();
    }
}
